package com.moneynote.chat.agent.general;

import org.springframework.stereotype.Component;

import com.moneynote.chat.agent.AbstractLlmAgent;

/**
 * 通用聊天 Agent：作为独立菜单项由用户主动选择，负责非 MoneyNote 账务类请求。
 * 它不是路由兜底目标——意图路由已移除，用户自己决定由哪个 Agent 回答。
 */
@Component
public class GeneralAgent extends AbstractLlmAgent {

    /** 全局唯一 Code：与注册表索引、前端菜单映射一致 */
    public static final String CODE = "GENERAL_CHAT";

    public GeneralAgent() {
        super(CODE, "prompts/general-chat/system.md");
    }

}
