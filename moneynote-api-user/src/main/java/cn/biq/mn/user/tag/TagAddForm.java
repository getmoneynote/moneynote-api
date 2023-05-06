package cn.biq.mn.user.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;


@Getter @Setter
public class TagAddForm {

    @NotNull
    private Integer bookId;

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

}
