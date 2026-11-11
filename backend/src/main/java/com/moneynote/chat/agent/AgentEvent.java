package com.moneynote.chat.agent;

import com.moneynote.chat.block.MessageBlock;

/**
 * Agent 层唯一的对外语言：一次生成过程中产生的领域事件。
 * 上层（编排层 → 聊天层）只认这个类型，不认 Spring AI 的 {@code ChatResponse}，
 * 也不认协议事件 {@code ChatEvent}——厂商 chunk 结构、思考内容的累计语义、
 * finishReason 的位置，全部由共享适配器（{@link ChatResponseProcessor} 等）吸收在 Agent 层内部。
 *
 * <p>sealed + record：类型全集固定，配合模式匹配 switch 不留 default，
 * 新增事件类型时编译器强制所有分发点同步处理，
 * 杜绝「加了事件但某个分发点静默丢弃」。
 *
 * <p><b>刻意不包含 Start / Complete / Error</b>（契约 R3）：Agent 无权宣布一次对话的
 * 终态。「成功」在本项目的定义是<b>落库成功</b>（{@code Message.STATUS_COMPLETED}），
 * 而 Agent 既不知道消息 id 也不知道事务边界。执行失败用<b>响应式流的 onError 信号</b>
 * 表达，由聊天层转成 error 事件。
 */
public sealed interface AgentEvent {

    /** 正文内容增量（原样转发，空格与换行都是有意义的输出） */
    record Content(String delta) implements AgentEvent {
    }

    /** 思考内容增量（已由 {@link ReasoningNormalizer} 归一为增量语义） */
    record Reasoning(String delta) implements AgentEvent {
    }

    /**
     * provider 上报的原始结束原因（stop / length / content_filter …），仅末 chunk 非空。
     * 属于模型输出元数据、不是终态判定：聊天层据此填充 finish 事件载荷后向上一路透出，
     * 自身不产生独立的 SSE 事件。
     */
    record FinishReason(String reason) implements AgentEvent {
    }

    /** 展示块事件（图表/表单等自定义内容），随文本流之后到达。 */
    record Block(MessageBlock block) implements AgentEvent {
    }

}
