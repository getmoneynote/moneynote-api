package cn.biq.mn.user.book;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.AccountCurrencyCodeField;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;

@Getter @Setter
public class BookAddForm {

    @NotBlank
    @NameField
    private String name;

    @NotBlank
    @AccountCurrencyCodeField
    private String defaultCurrencyCode;

    private Integer defaultExpenseAccountId;
    private Integer defaultIncomeAccountId;
    private Integer defaultTransferFromAccountId;
    private Integer defaultTransferToAccountId;
    private Integer defaultExpenseCategoryId;
    private Integer defaultIncomeCategoryId;

    @NotesField
    private String notes;

}
