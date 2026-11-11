package com.moneynote.chat.block;

/**
 * 表单块的提交动作标识：前端按它渲染表单、后端按它分发提交（见
 * {@code ChatBlockActionService}）。前后端契约的唯一定义点，不要在各处写字符串字面量。
 *
 * <p>新增动作时：这里加常量 → 生产工具用新动作造块 → 动作服务加分发分支 →
 * 前端 {@code FormBlock} 加渲染分支（四处同步，缺一处就会报「不支持的动作」）。
 */
public final class BlockActions {

    /** 新建一笔账单：字段口径见 {@code BookkeepingDraftTool}，确认后调用 MoneyFlowService.create */
    public static final String MONEY_FLOW_CREATE = "money-flow.create";

    private BlockActions() {
    }

}
