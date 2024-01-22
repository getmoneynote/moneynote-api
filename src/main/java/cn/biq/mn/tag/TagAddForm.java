package cn.biq.mn.tag;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class TagAddForm {

    @NotBlank
    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

    @NotNull
    private Boolean canExpense;

    @NotNull
    private Boolean canIncome;

    @NotNull
    private Boolean canTransfer;

    private Integer sort;

}
