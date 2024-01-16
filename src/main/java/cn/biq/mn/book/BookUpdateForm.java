package cn.biq.mn.book;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BookUpdateForm {

    @NameField
    private String name;

    private Integer defaultExpenseAccountId;
    private Integer defaultIncomeAccountId;
    private Integer defaultTransferFromAccountId;
    private Integer defaultTransferToAccountId;
    private Integer defaultExpenseCategoryId;
    private Integer defaultIncomeCategoryId;

    @NotesField
    private String notes;

    private Integer sort;

}
