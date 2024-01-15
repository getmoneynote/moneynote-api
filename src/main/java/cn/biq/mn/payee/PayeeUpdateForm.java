package cn.biq.mn.payee;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import lombok.Getter;
import lombok.Setter;


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
