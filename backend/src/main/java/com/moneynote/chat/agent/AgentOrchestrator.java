package com.moneynote.chat.agent;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

/**
 * Agent 调度中心：按菜单选定的 code 查注册表并装配冷流，是编排层的统一入口。
 * 不包含任何具体业务，也不知道每个 Agent 的实现细节，只依赖 {@link Agent} 接口——
 * 以后增加预算、投资等 Agent，本类无需改动（前端菜单与注册表各加一条即可）。
 * 消息生命周期/SSE/持久化仍归 MessageService（Chat 层），职责分离（R5）。
 *
 * <p>不再做意图判断：原来由 RouterAgent 做的「选人」已经上移到用户（左侧菜单选 Agent），
 * 因此这里没有额外的 LLM 调用，也就没有首字延迟与路由误判。
 *
 * <p>对外只吐 {@link AgentEvent}：编排层不感知协议事件、不分配 seq、不碰持久化。
 */
@Service
@RequiredArgsConstructor
public class AgentOrchestrator {

    private final AgentRegistry agentRegistry;

    /**
     * 按 code 取 Agent 并装配流：只做「查找 + 调用」，不订阅流——返回的 {@code events}
     * 是冷流，真正发起模型调用发生在调用方订阅时。code 恒来自菜单（已由
     * {@link AgentRegistry#requireRegistered(String)} 在请求入口校验白名单）。
     */
    public AgentExecution dispatch(AgentRequest request, AgentContext context, String agentCode) {
        Agent agent = agentRegistry.get(agentCode);
        return new AgentExecution(agentCode, agent.stream(request, context));
    }

}
