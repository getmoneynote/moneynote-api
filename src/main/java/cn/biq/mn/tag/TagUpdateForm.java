package cn.biq.mn.tag;

import cn.biq.mn.validation.NameField;
import cn.biq.mn.validation.NotesField;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;


@Getter @Setter
public class TagUpdateForm {

    @NameField
    private String name;

    @NotesField
    private String notes;

    @JsonProperty("pId")
    private Integer pId;

    private Boolean canExpense;

    private Boolean canIncome;

    private Boolean canTransfer;

    private Integer sort;

}
