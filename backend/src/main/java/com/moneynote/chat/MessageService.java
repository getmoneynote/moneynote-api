package com.moneynote.chat;

import java.time.Instant;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Flux;

import com.moneynote.chat.agent.AgentContext;
import com.moneynote.chat.agent.AgentExecution;
import com.moneynote.chat.agent.AgentOrchestrator;
import com.moneynote.chat.agent.AgentRegistry;
import com.moneynote.chat.agent.AgentRequest;
import com.moneynote.chat.block.MessageBlocks;
import com.moneynote.chat.event.ChatEvent;
import com.moneynote.chat.stream.GenerationInput;
import com.moneynote.chat.stream.GenerationPipeline;
import com.moneynote.llm.LlmClientFactory;
import com.moneynote.llm.LlmConfig;
import com.moneynote.user.CurrentUserHolder;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final LlmClientFactory llmClientFactory;
    private final ChatTransactionService chatTransactionService;
    private final AgentOrchestrator agentOrchestrator;
    private final AgentRegistry agentRegistry;
    private final GenerationPipeline generationPipeline;

    /**
     * 读取一条 Agent 固定时间线的历史。
     *
     * <p><b>【后期扩展点 · 上下文窗口】</b>当前返回<b>全量</b>历史（与模型输入口径一致），
     * 前端 Bubble.List 会随消息增长线性膨胀。将来加窗口/分页时要和
     * {@code ChatTransactionService.initialize} 的模型窗口一起改，保证用户看到的
     * 与模型看到的是同一段历史。详见 {@link Message} 类注释。
     */
    @Transactional(readOnly = true)
    public List<MessageListItem> list(String agentCode) {
        String code = agentRegistry.requireRegistered(agentCode);
        return messageRepository.findByUser_IdAndAgentCodeOrderByCreatedAtAsc(CurrentUserHolder.get(), code)
                .stream()
                .map(this::toListItem)
                .toList();
    }

    /** 清空当前用户在指定 Agent 下的整条时间线（删除必须带 userId，不能按 code 全表删）。 */
    @Transactional
    public void clear(String agentCode) {
        String code = agentRegistry.requireRegistered(agentCode);
        chatTransactionService.clear(CurrentUserHolder.get(), code);
    }

    /**
     * 发送一条用户消息，返回统一事件协议 {@link ChatEvent} 的流式回复。
     *
     * <p>本类只做<b>入口装配</b>：白名单校验 → 解析当前 LLM 配置 → 阶段一初始化短事务
     * → 组装 Agent 上下文与冷流 → 交给 {@link GenerationPipeline} 编排。
     * 流式管线、协议构造、事件映射、终态落库分别见
     * {@code chat.stream} 与 {@code chat.event} 包，本类不再持有任何 Reactor 细节。
     *
     * <p>所有依赖请求 {@code ThreadLocal} 的取值（LLM 配置、userId、ChatClient）都在
     * 本方法内即请求线程上完成，并随 {@link AgentContext} / {@link GenerationInput}
     * 显式传递；订阅线程不会回查 {@code CurrentUserHolder}。
     * 未知 agentCode 必须在流开始前、任何写库之前拒绝，否则 SSE 已建立就只能走
     * error 事件，拿不到标准错误体。
     *
     * <p>本方法无事务注解：阶段一 initialize 是独立短事务（返回时已提交），
     * 模型生成不持有事务，阶段三终态由 pipeline 各自开独立短事务。
     */
    public Flux<ChatEvent> send(String agentCode, MessageSendForm form) {
        String code = agentRegistry.requireRegistered(agentCode);

        LlmConfig config = llmClientFactory.currentConfig();
        ChatClient client = llmClientFactory.currentClient();
        Long userId = CurrentUserHolder.get();

        ChatContext context = chatTransactionService.initialize(
                userId, code, form.getContent(), config.getModel());

        AgentContext agentContext = new AgentContext(userId, null, Instant.now(), client);
        AgentExecution execution = agentOrchestrator.dispatch(
                new AgentRequest(context.history()), agentContext, code);

        return generationPipeline.run(new GenerationInput(
                context.assistantMessageId(), code, config.getModel(), execution));
    }

    private MessageListItem toListItem(Message message) {
        MessageListItem item = new MessageListItem();
        item.setId(message.getId());
        item.setRole(message.getRole());
        item.setContent(message.getContent());
        item.setParts(message.getParts() == null ? null
                : MessageBlocks.fromJson(message.getParts()));
        item.setReasoningContent(message.getReasoningContent());
        item.setModel(message.getModel());
        item.setAgentCode(message.getAgentCode());
        item.setStatus(message.getStatus());
        item.setCreatedAt(message.getCreatedAt());
        return item;
    }

}
