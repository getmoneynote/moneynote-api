package cn.biq.mn.account;

import cn.biq.mn.validation.NotesField;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class AccountUpdateNotesForm {

    @NotesField
    private String notes;

}
