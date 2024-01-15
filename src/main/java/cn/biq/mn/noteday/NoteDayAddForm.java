package cn.biq.mn.noteday;

import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

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
