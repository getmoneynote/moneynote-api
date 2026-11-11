package com.moneynote.chat.agent.bookkeeping;

import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import com.moneynote.chat.agent.AbstractLlmAgent;
import com.moneynote.chat.agent.AgentContext;
import com.moneynote.chat.block.BlockCollector;

/**
 * 记账 Agent：整理记账要素、生成待确认的记账表单、解答记账问题。
 *
 * <p>只挂 {@link BookkeepingDraftTool}：工具不写库，只把要素包成表单块等用户确认，
 * 真正的落库发生在表单提交端点（用户主动确认之后）。写库方法绝不注册给 LLM。
 */
@Component
public class BookkeepingAgent extends AbstractLlmAgent {

    /** 全局唯一 Code：与注册表索引、前端菜单映射一致 */
    public static final String CODE = "BOOKKEEPING";

    public BookkeepingAgent() {
        super(CODE, "prompts/bookkeeping/system.md");
    }

    /**
     * 每次执行新建工具实例：工具持有本次执行的块收集器与时间基准，做成单例会把
     * 用户 A 的表单块串到用户 B 的消息里。
     */
    @Override
    public List<ToolCallback> tools(AgentContext context, BlockCollector blocks) {
        if (context == null) {
            return List.of();
        }
        return List.of(ToolCallbacks.from(new BookkeepingDraftTool(blocks, context.now())));
    }

}
