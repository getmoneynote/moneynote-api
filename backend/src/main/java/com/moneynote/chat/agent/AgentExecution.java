package com.moneynote.chat.agent;

import reactor.core.publisher.Flux;

/**
 * 一次调度的产物：选中的 Agent Code + 它的事件流。
 *
 * <p>为什么不直接返回 {@code Flux<AgentEvent>}：聊天层必须<b>先知道 agentCode</b>
 * （回填消息归属、然后才发 start 事件），而 agentCode 来自路由决策——
 * 若只返回一条流，聊天层就得在流里猜或者等第一个事件，顺序会被打乱。
 * 拆成两项后，装配（拿到 code 并回填）与订阅（开始生成）依然分离。
 *
 * @param agentCode 菜单选定的 Agent Code（恒为已注册 Agent，由请求入口白名单校验）
 * @param events    该 Agent 的领域事件流（冷流，订阅才触发模型调用）
 */
public record AgentExecution(String agentCode, Flux<AgentEvent> events) {

}
