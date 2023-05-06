package cn.biq.mn.user.payee;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;


@Getter
@Setter
public class PayeeUpdateForm {

    @NameField
    private String name;

    @NotesField
    private String notes;

    private Boolean canExpense;

    private Boolean canIncome;

}
