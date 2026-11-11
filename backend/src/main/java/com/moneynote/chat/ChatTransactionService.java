package com.moneynote.chat;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.moneynote.chat.block.MessageBlock;
import com.moneynote.chat.block.MessageBlocks;
import com.moneynote.user.User;
import com.moneynote.user.UserRepository;

/**
 * 聊天事务边界服务：持有发送流程中所有数据库短事务。
 * <ul>
 *   <li>阶段一 {@link #initialize}：初始化（USER 消息 + GENERATING 占位 + 历史加载），
 *       模型流式生成（阶段二）不在此事务内；</li>
 *   <li>阶段三 {@link #complete} / {@link #fail} / {@link #stop}：终态落库，各自独立短事务，
 *       模型生成的成败不影响阶段一已提交的消息，失败/中止保留已生成部分；</li>
 *   <li>{@link #clear}：清空一条 Agent 固定时间线；</li>
 *   <li>{@link #sweepStaleGenerating}：启动清扫，兜底无任何终态落库机会的
 *       GENERATING 残留（进程重启等）。</li>
 * </ul>
 *
 * 为什么独立成 Service：{@code @Transactional} 依赖 Spring 代理生效，若作为
 * MessageService 的内部方法被 this. 自调用会绕过代理导致事务静默失效；
 * 独立 Bean 让"模型生成不持有数据库事务"成为结构约束而非注释约定。
 * 终态方法运行在流回调线程上，注解事务按调用线程新建，不依赖请求 ThreadLocal。
 */
@Service
@RequiredArgsConstructor
public class ChatTransactionService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /**
     * 阶段一初始化短事务：保存 USER 消息 + GENERATING 占位 assistant 消息，
     * 并加载该 Agent 时间线的历史。方法返回时事务已提交，
     * 模型生成的成败不影响这两条消息的存在（生成失败由终态事务落 ERROR，
     * 见 MessageService.persistTerminal）。
     *
     * <p>没有会话行：两条消息直接带 {@code (userId, agentCode)} 分区键，
     * agentCode 由菜单选定（请求路径），因此不需要原来的 assignAgentCode 二次回填，
     * 也不会再生成会话标题 / 触碰 updatedAt。
     *
     * 历史加载也放进事务阶段：send() 后续组装段因此不再触碰数据库
     * （纯内存操作），不会在事务提交后因组装失败留下 GENERATING 孤儿。
     *
     * @param userId 调用方在请求线程上取好的当前用户（本方法也在请求线程执行，
     *               但显式传参便于脱离 ThreadLocal 复用与测试）
     */
    @Transactional
    public ChatContext initialize(Long userId, String agentCode, String content, String model) {
        User user = userRepository.getReferenceById(userId);

        Message userMessage = new Message();
        userMessage.setUser(user);
        userMessage.setAgentCode(agentCode);
        userMessage.setRole(Message.ROLE_USER);
        userMessage.setContent(content);
        messageRepository.save(userMessage);

        // 分阶段持久化：先落一条 GENERATING 的 assistant 消息拿 messageId（终态时更新而非新增）
        Message assistantMessage = new Message();
        assistantMessage.setUser(user);
        assistantMessage.setAgentCode(agentCode);
        assistantMessage.setRole(Message.ROLE_ASSISTANT);
        assistantMessage.setContent("");
        assistantMessage.setModel(model);
        assistantMessage.setStatus(Message.STATUS_GENERATING);
        messageRepository.save(assistantMessage);

        // 历史加载 + Entity→DTO 转换都放进事务阶段：转换在持久化上下文内完成，
        // 访问字段绝对安全；事务外拿到的只是不含 Hibernate 关联的只读 DTO，
        // send() 后续组装段因此不再触碰数据库（纯内存操作），也不会因组装
        // 失败留下 GENERATING 孤儿。
        //
        // 过滤规则：排除刚落的 GENERATING 占位（空 content 的 assistant 消息
        // 会被多数 OpenAI 兼容 API 以 400 拒绝）；status 为 null 的旧数据
        // （分阶段持久化之前的历史消息）视为已完成，正常保留。
        //
        // 【后期扩展点 · 上下文窗口】这里当前是「全量历史 → 模型输入」，一个 Agent
        // 一条永久时间线，消息只增不减，token 成本与首字延迟会随使用线性增长。
        // 将来加窗口时截断就发生在本段（排除 GENERATING 占位之后、构造 DTO 之前）：
        // ① Repository 改 findTopNBy...OrderByCreatedAtDesc 并反转为升序；
        // ② 截断后首条必须是 USER，否则部分 OpenAI 兼容端点会报 400；
        // ③ 与 MessageService.list 的前端展示口径保持一致。
        // 本版刻意不实现，只留扩展点；详见 Message / MessageRepository 类注释。
        List<ChatHistoryMessage> history = messageRepository
                .findByUser_IdAndAgentCodeOrderByCreatedAtAsc(userId, agentCode)
                .stream()
                .filter(m -> m.getStatus() == null || m.getStatus() != Message.STATUS_GENERATING)
                .map(m -> {
                    ChatHistoryMessage item = new ChatHistoryMessage();
                    item.setRole(m.getRole());
                    // ASSISTANT 消息若有 parts，把摘要拼进模型输入（方案 B 口径）：
                    // 块数据不回灌，模型只看到工具生成的确定性摘要
                    String historyContent = m.getContent();
                    if (m.getRole() == Message.ROLE_ASSISTANT && m.getParts() != null) {
                        String summary = MessageBlocks.appendSummaries(m.getParts());
                        if (!summary.isBlank()) {
                            historyContent = historyContent + summary;
                        }
                    }
                    item.setContent(historyContent);
                    return item;
                })
                .toList();

        return new ChatContext(userMessage.getId(), assistantMessage.getId(), history);
    }

    /**
     * 清空当前用户在指定 Agent 下的整条时间线（「清空记录」）。
     * 前端应先中止进行中的流、等终态落库完成后再调用；即便终态回调稍后到达，
     * persistTerminal 对已被删除的消息行做了静默跳过，不会抛异常或写出幽灵消息。
     */
    @Transactional
    public void clear(Long userId, String agentCode) {
        messageRepository.deletePartition(userRepository.getReferenceById(userId), agentCode);
    }

    /**
     * 阶段三终态落库（正常完成）：保存完整回复并置 COMPLETED。
     * COMPLETED 的语义 = 最终结果已成功持久化——状态与 content 在同一事务写入，
     * 本方法正常返回（事务提交）才算完成，调用方据此发出 finish 事件。
     * 调用方负责用 persisted 标志保证唯一终态（见 MessageService.persistTerminal）。
     */
    @Transactional
    public void complete(Long messageId, String content, String reasoningContent,
                         List<MessageBlock> blocks) {
        persistTerminal(messageId, content, reasoningContent, blocks, Message.STATUS_COMPLETED);
    }

    /**
     * 阶段三终态落库（生成出错）：置 ERROR 并保留已生成部分，
     * 用户刷新后仍能看到失败前的内容，不静默丢弃。
     */
    @Transactional
    public void fail(Long messageId, String partialContent, String partialReasoningContent,
                     List<MessageBlock> blocks) {
        persistTerminal(messageId, partialContent, partialReasoningContent, blocks, Message.STATUS_ERROR);
    }

    /**
     * 阶段三终态落库（用户中止）：置 STOPPED 并保留已生成部分。
     * 当前触发源是 SSE 连接断开（doOnCancel），无法再向客户端发事件，
     * 因此状态只能落在数据库里，不能只依赖 SSE 事件。
     */
    @Transactional
    public void stop(Long messageId, String partialContent, String partialReasoningContent,
                     List<MessageBlock> blocks) {
        persistTerminal(messageId, partialContent, partialReasoningContent, blocks, Message.STATUS_STOPPED);
    }

    /**
     * 终态落库的共享实现：更新既有 GENERATING 消息（更新而非新增）。
     * 消息行可能已被「清空记录」删除（清空与中止/终态回调存在竞态），
     * 此时静默跳过即可——既不抛异常污染 error 事件，也不能重新插入幽灵消息。
     */
    private void persistTerminal(Long messageId, String content, String reasoningContent,
                                 List<MessageBlock> blocks, int status) {
        messageRepository.findById(messageId).ifPresent(message -> {
            message.setContent(content);
            // 思考内容仅在确实产生过时落库，避免给普通模型/无思考场景存空串
            message.setReasoningContent(reasoningContent);
            // 块序列化：空列表落 null，与无块消息形态一致
            message.setParts(MessageBlocks.toJson(blocks));
            message.setStatus(status);
        });
    }

    /**
     * 启动清扫：把 cutoff 之前创建、仍停在 GENERATING 的消息批量置为 ERROR。
     * 覆盖两类无终态场景：进程重启/被杀时正在生成的消息（complete/fail/stop
     * 与 doOnCancel 都随 JVM 消失，没有任何落终态的机会）；终态写库本身失败。
     * 仅翻转状态，已生成的部分内容原样保留。
     *
     * @return 被清扫的消息条数
     */
    @Transactional
    public int sweepStaleGenerating(Instant cutoff) {
        return messageRepository.markStatusBefore(
                Message.STATUS_GENERATING, Message.STATUS_ERROR, cutoff);
    }

}
