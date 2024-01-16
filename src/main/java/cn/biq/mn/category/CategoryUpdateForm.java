package cn.biq.mn.category;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CategoryUpdateForm {

    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

    private Integer sort;

}
