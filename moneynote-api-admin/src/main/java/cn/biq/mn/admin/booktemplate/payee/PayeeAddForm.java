package cn.biq.mn.admin.booktemplate.payee;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;


@Getter @Setter
public class PayeeAddForm {

    @NotNull
    private Integer bookId;

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

}
