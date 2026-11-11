package com.moneynote.chat.agent.account;

import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

import com.moneynote.chat.agent.AbstractLlmAgent;
import com.moneynote.chat.agent.AgentContext;
import com.moneynote.chat.block.BlockCollector;

/**
 * 资产分析 Agent：资产/负债/净资产与资产结构相关分析。
 *
 * <p>两个只读工具：{@link AccountBalanceTool} 取数、{@link AccountChartTool} 出图
 * （账户余额的计算、折算、口径过滤全在 {@link AccountAnalysisService} 里，
 * 工具本身只是把快照递给模型或映射成图表块）。
 */
@Component
public class AccountAgent extends AbstractLlmAgent {

    /** 全局唯一 Code：与注册表索引、前端菜单映射一致 */
    public static final String CODE = "ACCOUNT_ANALYSIS";

    private final AccountAnalysisService accountAnalysisService;

    public AccountAgent(AccountAnalysisService accountAnalysisService) {
        super(CODE, "prompts/account-analysis/system.md");
        this.accountAnalysisService = accountAnalysisService;
    }

    /**
     * 每次执行新建工具实例并绑定本次用户：工具在流管线线程上被调用，拿不到 ThreadLocal，
     * 单例复用会把用户 A 的查询结果串给用户 B。
     */
    @Override
    public List<ToolCallback> tools(AgentContext context, BlockCollector blocks) {
        if (context == null || context.userId() == null) {
            // 身份缺失时不挂工具：宁可让模型说明读不到数据，也不要拿 null 去查库
            return List.of();
        }
        // ToolCallbacks.from(Object...) 返回回调数组，两个工具要展开后再拼（不能塞进 List.of 的两参重载）
        List<ToolCallback> callbacks = new ArrayList<>();
        callbacks.addAll(List.of(ToolCallbacks.from(
                new AccountBalanceTool(accountAnalysisService, context.userId()))));
        callbacks.addAll(List.of(ToolCallbacks.from(
                new AccountChartTool(accountAnalysisService, context.userId(), blocks))));
        return callbacks;
    }

}
