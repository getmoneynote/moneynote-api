package com.moneynote.account;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.validator.constraints.Range;

import com.moneynote.validate.ValidNotes;

@Getter @Setter
public class AccountUpdateForm {

    @Size(max = 64)
    private String name;

    private Boolean include;

    private Boolean canExpense;

    private Boolean canIncome;

    private Boolean canTransferFrom;

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

    /** 显示排序，数字越小越靠前，可空；为 null 时跳过赋值（partial update 语义） */
    @Min(0)
    private Integer displayOrder;

    @ValidNotes
    private String notes;

}
