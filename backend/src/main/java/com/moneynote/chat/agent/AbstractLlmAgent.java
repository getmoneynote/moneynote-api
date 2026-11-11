package com.moneynote.chat.agent;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;

import reactor.core.publisher.Flux;

import com.moneynote.chat.block.BlockCollector;
import com.moneynote.utils.ClasspathResources;

/**
 * LLM 型专业 Agent 的骨架实现：收敛 code、System Prompt 加载、标准的
 * 「system + 会话历史」流式调用，以及<b>chunk → 领域事件</b>的转换。
 * 子类只需声明身份与提示词位置。
 *
 * <p>为什么转换放在这里而不是每个子类：{@link ChatResponseProcessor} 里藏着
 * 「Spring AI 2.0.2 跨 chunk 累计思考内容」这段适配知识（契约 R2），
 * 它只应该存在一份，由骨架统一调用；子类若各写一遍，升级 SDK 时就会漏改。
 *
 * <p>未来的差异化能力（各自 Tools、执行策略）在子类扩展：覆盖 {@link #stream}
 * 或提供 {@link #tools(AgentContext, BlockCollector)}，骨架不变。
 */
public abstract class AbstractLlmAgent implements Agent {

    private final String code;

    /**
     * 构造期从 classpath 加载（见 {@link ClasspathResources#readText(String)}），
     * 文件缺失立即失败（fail-fast），不带病运行
     */
    private final String systemPrompt;

    protected AbstractLlmAgent(String code, String promptClassPathLocation) {
        this.code = code;
        this.systemPrompt = ClasspathResources.readText(promptClassPathLocation);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public Flux<AgentEvent> stream(AgentRequest request, AgentContext context) {
        // 归一器有状态（跨 chunk 累计的思考内容），每次执行必须新建，
        // 不能做成字段/单例，否则并发请求之间会串线
        ChatResponseProcessor processor = new ChatResponseProcessor();
        // 每次执行现取工具：工具实例绑定本次 userId（见 Agent#tools），不能缓存复用
        BlockCollector blocks = new BlockCollector();
        List<ToolCallback> toolCallbacks = tools(context, blocks);
        ChatClient.ChatClientRequestSpec spec = context.client().prompt()
                .system(systemPrompt)
                // history 已含当前用户消息，无需再拼 user()
                .messages(AgentRequest.toAiMessages(request.history()));
        if (!toolCallbacks.isEmpty()) {
            // 空列表不挂：未接工具的 Agent 走原路径，行为零变化。
            // Spring AI 2.0 起 toolCallbacks(...) 已废弃（3.0 移除），统一改走 tools(Object...)；
            // 传入 List 时框架按 Collection 逐项分派，与旧的 toolCallbacks(List) 完全等价
            spec = spec.tools(toolCallbacks);
        }
        // 块在文本流之后到达：工具在流中间执行完毕，上游 concatMapIterable
        // 完成后再 drain，保证展示块排在文字之后（与前端渲染模型自洽）。
        return spec.stream()
                .chatResponse()
                // 一个 chunk 可展开成 0~3 个事件（先思考、后正文、末 chunk 带结束原因），
                // concatMapIterable 保证 chunk 之间与 chunk 内部的顺序都不变
                .concatMapIterable(processor::process)
                .concatWith(Flux.defer(() ->
                        Flux.fromIterable(blocks.snapshot()).map(AgentEvent.Block::new)));
    }

}
