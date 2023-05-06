package cn.biq.mn.user.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;

@Getter @Setter
public class CategoryUpdateForm {

    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

}
