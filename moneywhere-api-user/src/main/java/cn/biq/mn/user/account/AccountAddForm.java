package cn.biq.mn.user.account;

import cn.biq.mn.base.validation.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class AccountAddForm {

    @NotNull
    private AccountType type;

    @NotBlank
    @NameField
    private String name;

    @AccountNoField
    private String no;

    @NotNull
    @AmountField
    private BigDecimal balance;

    @NotNull
    private Boolean include;

    @NotNull
    private Boolean canTransferFrom;

    @NotNull
    private Boolean canTransferTo;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

    @NotesField
    private String notes;

    @AccountCurrencyCodeField
    private String currencyCode;

    @AmountField
    private BigDecimal creditLimit;

    @BillDayField
    private Integer billDay;

    @Digits(integer = 4, fraction = 4)
    private BigDecimal apr;

}
