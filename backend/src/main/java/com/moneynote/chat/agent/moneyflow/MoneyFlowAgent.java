package com.moneynote.chat.agent.moneyflow;

import org.springframework.stereotype.Component;

import com.moneynote.chat.agent.AbstractLlmAgent;

/**
 * 收支分析 Agent：一定时间范围内的收入/支出/结余统计分析。
 * 当前为基础版（提示词 persona，未接账单统计工具），工具接入后在此扩展。
 */
@Component
public class MoneyFlowAgent extends AbstractLlmAgent {

    /** 全局唯一 Code：与注册表索引、前端菜单映射一致 */
    public static final String CODE = "MONEY_FLOW_ANALYSIS";

    public MoneyFlowAgent() {
        super(CODE, "prompts/moneyflow-analysis/system.md");
    }

}
