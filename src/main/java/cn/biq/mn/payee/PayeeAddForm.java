package cn.biq.mn.payee;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class PayeeAddForm {

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

    private Integer sort;

}
