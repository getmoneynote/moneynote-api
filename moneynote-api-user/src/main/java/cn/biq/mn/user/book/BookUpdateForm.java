package cn.biq.mn.user.book;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;

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

}
