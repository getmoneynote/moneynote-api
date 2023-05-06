package cn.biq.mn.user.noteday;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.TitleField;

@Getter @Setter
public class NoteDayAddForm  {

    @NotBlank
    @TitleField
    private String title;

    @NotesField
    private String notes;

    @NotNull
    @TimeField
    private Long startDate;

    @TimeField
    private Long endDate;

    @NotNull
    private Integer repeatType;

    private Integer interval;

}
