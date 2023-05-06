package cn.biq.mn.user.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NameField;
import cn.biq.mn.base.validation.NotesField;


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

}
