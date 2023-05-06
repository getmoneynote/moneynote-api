package cn.biq.mn.admin.booktemplate.category;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;

@Getter @Setter
public class CategoryAddForm {

    @NotNull
    private Integer bookId;

    @NotNull
    private CategoryType type;

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

}
