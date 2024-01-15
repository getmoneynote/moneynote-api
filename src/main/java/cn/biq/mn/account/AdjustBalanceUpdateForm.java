package cn.biq.mn.account;

import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AdjustBalanceUpdateForm {

    private Integer bookId;

    @NotNull
    @TimeField
    private Long createTime;

    @TitleField
    private String title;

    @NotesField
    private String notes;

}
