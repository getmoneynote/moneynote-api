package com.moneynote.moneyflow;

import com.moneynote.validate.ValidNotes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import java.util.List;

@Getter @Setter
public class MoneyFlowCreateForm {

    /**
     * 归属账本；为空默认当前账本。
     */
    @NotNull
    private Long bookId;

    /**
     * 流水类型：1=支出 2=收入 3=转账。
     */
    @NotNull
    @Range(min = 1, max = 3)
    private Integer type;

    @Size(max = 32)
    private String title;

    /**
     * 发生时间（Unix 毫秒），取值范围与 reminder.executeDate 一致。
     */
    @NotNull
    @Min(value = 0, message = "{validation.timestamp.invalid}")
    @Max(value = 32503680000000L, message = "{validation.timestamp.invalid}") // 3000-01-01T00:00:00Z
    private Long createTime;

    /**
     * 支出/收入的主账户，可空（空 = 未归属/待核对）；转账时为转出账户，必填。
     */
    private Long accountId;

    /**
     * 原币金额（最小货币单位）。accountId 非空时与账户币种一致，为空时按账本币种计量。
     * 支出/收入允许负数（负数 = 退款记录：负支出回加余额、负收入回减余额）；
     * 转账必须非负，由 Service 层校验（不放在表单注解上，因为放行与否取决于 type）。
     */
    @NotNull
    private Long amount;

    /**
     * 换算金额（最小货币单位，用户输入）：支出/收入为账本币种金额，转账为转入账户到账金额。
     * 币种一致时可不传（后端按 amount 处理），不一致时必填；是否必填由后端按币种关系判定。
     * 符号规则同 amount：收支可负（退款），转账非负由 Service 层校验。
     */
    private Long convertedAmount;

    @ValidNotes
    private String notes;

    /**
     * 分类：支出/收入必填；转账忽略（实体上 category 对转账可为空）。
     */
    private Long categoryId;

    /**
     * 标签 id 列表，可空。
     */
    private List<Long> tagIds;

    /**
     * 转入账户：仅转账必填。
     */
    private Long toAccountId;

    /**
     * 交易对象，可空。
     */
    private Long payeeId;

    /**
     * 是否已确认；确认后才会执行账户余额操作（见 MoneyFlow.confirm）。
     */
    @NotNull
    private Boolean confirm;

    /**
     * 是否计入统计。
     */
    @NotNull
    private Boolean include;

}