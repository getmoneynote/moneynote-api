package cn.biq.mn.book;

import cn.biq.mn.validation.AccountCurrencyCodeField;
import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

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

    @NotesField
    private String notes;

    private Integer sort;

}
