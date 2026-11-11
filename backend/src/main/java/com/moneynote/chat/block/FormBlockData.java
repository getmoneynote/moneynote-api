package com.moneynote.chat.block;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * 表单块数据：一个待用户确认/补全的业务表单。
 *
 * <ul>
 *   <li>{@code action} 决定提交时后端走哪个动作（当前仅 money-flow.create），
 *       前端按 action 渲染对应的专用表单；</li>
 *   <li>{@code fields} 是模型预填的初始值（主单位金额、可空的账户/分类 id 等），
 *       用户可在提交前修改，提交时整组回传为 values；</li>
 *   <li>{@code status} 随提交闭环流转（见 {@link FormStatus}），提交成功后
 *       {@code resultText} 承载只读结果文案。</li>
 * </ul>
 * 不用 record：提交闭环要原地改写 status/resultText，可变类更直白。
 */
@Getter @Setter
public final class FormBlockData implements BlockData {

    /** 提交动作标识（当前仅 money-flow.create），前后端与动作分发共用的契约 */
    private String action;

    /** 表单标题（如「记账确认」） */
    private String title;

    /** 模型预填的字段初始值；键集合与提交动作的 values 契约一致 */
    private Map<String, Object> fields;

    /** 流转状态，见 {@link FormStatus} */
    private FormStatus status;

    /** 提交成功后的只读结果文案（如「已记账：午餐 35 CNY」），pending 时为 null */
    private String resultText;

}
