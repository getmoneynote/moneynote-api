package cn.biq.mn.admin.booktemplate.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;

@Getter @Setter
public class CategoryUpdateForm {

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

}
