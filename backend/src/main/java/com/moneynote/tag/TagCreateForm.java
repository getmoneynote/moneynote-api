package com.moneynote.tag;

import com.moneynote.validate.ValidName;
import com.moneynote.validate.ValidNotes;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagCreateForm {

    @ValidName
    private String name;

    @ValidNotes
    private String notes;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

    @NotNull
    private Boolean canTransfer;

}
