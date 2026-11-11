package com.moneynote.chat.stream;

import com.moneynote.chat.agent.AgentExecution;

/**
 * 一次生成的入参，{@code MessageService} 与 {@link GenerationPipeline} 之间唯一的边界。
 * 所有数据库访问、ThreadLocal 读取都已在请求线程完成，本对象只含纯内存数据。
 *
 * <p>不含 userId：阶段一 initialize 已把分区键写好，终态落库只按 messageId 定位。
 *
 * @param messageId 阶段一落下的 GENERATING 占位 assistant 消息 id
 * @param agentCode 菜单选定的 Agent（同时是消息分区键）
 * @param model     本次使用的模型快照，写入 start 载荷
 * @param execution Agent 调度产物（冷流，订阅才发起模型调用）
 */
public record GenerationInput(
        Long messageId,
        String agentCode,
        String model,
        AgentExecution execution) {

}
