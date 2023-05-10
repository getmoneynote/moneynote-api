package cn.biq.mn.user.group;

import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class GroupUpdateForm {

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

}
