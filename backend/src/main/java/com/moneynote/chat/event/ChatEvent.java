package com.moneynote.chat.event;

/**
 * 聊天流式统一事件协议（SSE 载荷）信封，纯数据、无行为。
 *
 * <p>约定：SSE 的 {@code event:} 字段与 JSON 内 {@code eventType} 一致，均取
 * {@link ChatEventType#wireName()}。构造一律走 {@link ChatEventFactory}（唯一发号与
 * 配对校验点），事件类型全集见 {@link ChatEventType}，各类型载荷见
 * {@code com.moneynote.chat.event.payload}。
 *
 * <p>载荷形如 {@code {eventType, messageId, seq, eventData}}。<b>组件名即 Jackson
 * 的 JSON 字段名，严禁重命名</b>；会话实体移除后不再有 conversationId，一条固定
 * 时间线由 {@code /chat/agents/{agentCode}/messages} 唯一确定，start 载荷里的
 * agentCode 即分区键。
 *
 * <p>唯一终态原则：一次生成只允许出现 finish 或 error 之一（用户中止对应落库
 * STOPPED，无事件）。事件全集：start / reasoning / content / block / finish / error，
 * 其中 block 携带一条结构化展示块（图表/表单），在文本流之后到达。
 */
public record ChatEvent(
        ChatEventType eventType,
        Long messageId,
        int seq,
        Object eventData) {

}
