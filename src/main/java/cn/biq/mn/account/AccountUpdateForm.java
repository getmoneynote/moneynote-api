package cn.biq.mn.account;

import cn.biq.mn.validation.*;
import jakarta.validation.constraints.Digits;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class AccountUpdateForm {

    @NameField
    private String name;

    @AccountNoField
    private String no;

    @NotesField
    private String notes;

    @AmountField
    private BigDecimal creditLimit;

    @BillDayField
    private Integer billDay;

    private Boolean include;

    private Boolean canTransferFrom;

    private Boolean canTransferTo;

    private Boolean canExpense;

    private Boolean canIncome;

    @Digits(integer = 4, fraction = 4)
    private BigDecimal apr;

    private Integer sort;

}
