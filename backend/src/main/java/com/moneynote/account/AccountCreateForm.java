package com.moneynote.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Range;

import com.moneynote.validate.ValidName;
import com.moneynote.validate.ValidNotes;

@Getter @Setter
public class AccountCreateForm {

    @ValidName
    private String name;

    /**
     * 账户类型：1-活期 2-信用 3-资产 4-贷款。
     * 范围校验在 AccountService.create 中做，此处仅 @NotNull
     */
    @NotNull
    private Integer type;

    @NotNull
    private Long currencyId;

    @NotNull
    private Long balance;

    @NotNull
    private Boolean include;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

    @NotNull
    private Boolean canTransferFrom;

    @NotNull
    private Boolean canTransferTo;

    @Size(max = 32)
    private String no;

    @Min(0)
    private Long creditLimit;

    @Range(min = 1, max = 31)
    private Integer billDay;

    /** 年化利率(%)，与 Account.apr 列的 precision=7/scale=4 对齐 */
    @DecimalMin(value = "0")
    @Digits(integer = 3, fraction = 4)
    private BigDecimal apr;

    /** 显示排序，数字越小越靠前，可空（空排在后面） */
    @Min(0)
    private Integer displayOrder;

    @ValidNotes
    private String notes;

}
