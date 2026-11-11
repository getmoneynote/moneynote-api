package com.moneynote.chat.agent;

import java.util.List;

import org.springframework.ai.tool.ToolCallback;

import reactor.core.publisher.Flux;

import com.moneynote.chat.block.BlockCollector;

/**
 * 专业 Agent 的统一抽象：所有业务 Agent 遵循同一执行规范，
 * Orchestrator / Registry 只依赖本接口，不感知具体实现。
 *
 * <p><b>契约（R1）：Agent 只表达领域事件。</b>返回值里不允许出现
 * {@code ChatResponse}、{@code ChatEvent}、{@code ServerSentEvent}——
 * 厂商 chunk 结构、思考内容累计语义、finishReason 位置由共享适配器吸收在
 * {@link AbstractLlmAgent} 内部，对外统一用 {@link AgentEvent} 表达，
 * 上层无需回头读 Spring AI 的内部结构。
 *
 * <p>Agent 本身不持有 ChatClient：模型客户端来自当前用户激活的 LLM 配置
 * （{@code LlmClientFactory.currentClient()}，依赖请求 ThreadLocal），
 * 由调用方在请求线程上解析后放进 {@link AgentContext} 传入——与
 * ChatTransactionService 显式传 userId 的约定一致，避免 Agent 内部
 * 在非请求线程上误触 ThreadLocal。
 *
 * <p>边界（R5）：Agent 不创建/保存消息、不建 SSE 连接、不决定会话标题、不做取消处理，
 * 也不宣布终态（R3，成功与否由落库结果决定）。执行失败用响应式流的 onError 表达。
 */
public interface Agent {

    /** 全局唯一 Code（如 BOOKKEEPING）：注册表索引、菜单映射与消息分区键共用 */
    String getCode();

    /**
     * 执行本次生成，返回领域事件流（冷流：装配时不发起请求，订阅才触发模型调用）。
     * request 的 history 已包含当前这条用户消息（阶段一事务随历史一并加载），
     * 实现方无需再单独拼 user 消息。
     */
    Flux<AgentEvent> stream(AgentRequest request, AgentContext context);

    /**
     * 本次可用的工具：未接工具的 Agent 返回空列表（默认）。
     *
     * <p>为什么带 {@link AgentContext} 和 {@link BlockCollector}：
     * 工具要读写「本次执行的用户/账本」；展示块（图表/表单）由工具经由 collector 产出，
     * 回调线程上不能回头引用上下文——Collector 在<b>请求线程</b>创建、交给工具实例，
     * 工具在流管线线程上回调，两者只在订阅线程持有。
     *
     * <p>实现方在请求线程上用 context 构造新的工具实例（每次执行 new，不做单例），
     * 工具内部只认构造参数，绝不回头查 ThreadLocal。
     *
     * <p>R6：写库方法绝不注册给 LLM。
     */
    default List<ToolCallback> tools(AgentContext context, BlockCollector blocks) {
        return List.of();
    }

}
