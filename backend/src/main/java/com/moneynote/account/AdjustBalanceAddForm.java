package com.moneynote.account;

import com.moneynote.validate.ValidNotes;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdjustBalanceAddForm {

    /**
     * 发生时间（Unix 毫秒），取值范围与 reminder.executeDate 一致。
     */
    @NotNull
    @Min(value = 0, message = "{validation.timestamp.invalid}")
    @Max(value = 32503680000000L, message = "{validation.timestamp.invalid}") // 3000-01-01T00:00:00Z
    private Long createTime;

    @Size(max = 32)
    private String title;

    @ValidNotes
    private String notes;

    /**
     * 目标余额（最小货币单位），把账户余额覆盖为该值；允许为负（信用/贷款账户）。
     */
    @NotNull
    private Long balance;

    /**
     * 是否计入统计：决定该条余额调整流水是否参与账单统计（对应 MoneyFlow.include）。
     */
    @NotNull
    private Boolean include;

}