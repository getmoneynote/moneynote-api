package com.moneynote.chat;

import java.util.List;

/**
 * 阶段一初始化事务（{@link ChatTransactionService#initialize}）的产出：
 * 流式生成所需的全部数据库上下文。send() 的后续组装段只消费这个上下文，
 * 不再触碰数据库——"模型生成不持有数据库事务"由此在结构上成立。
 *
 * <p>没有会话 id：agentCode 由菜单选定、随请求路径传入，USER 与 GENERATING 占位消息
 * 在 initialize 里一次性写好分区键，无需路由后再回填（原 assignAgentCode 已删除）。
 *
 * history 是事务提交后的消息快照，已转换为 {@link ChatHistoryMessage} DTO
 * （不含 JPA Entity，杜绝 detached 实体的懒加载风险与意外写库），
 * 且已排除 GENERATING 占位消息（空 content 的 assistant 消息会被
 * 多数 OpenAI 兼容 API 拒绝）。
 *
 * userMessageId 当前只作为初始化产出的定位信息保留（写入分区键后不再二次更新）。
 */
public record ChatContext(
        Long userMessageId,
        Long assistantMessageId,
        List<ChatHistoryMessage> history) {

    public ChatContext {
        history = List.copyOf(history);
    }
}
