package cn.biq.mn.group;

import cn.biq.mn.validation.AccountCurrencyCodeField;
import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupAddForm {

    @NotBlank
    @NameField
    private String name;

    @NotBlank
    @AccountCurrencyCodeField
    private String defaultCurrencyCode;

    @NotesField
    private String notes;

}
