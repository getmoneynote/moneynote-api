package cn.biq.mn.noteday;

import cn.biq.mn.validation.NotesField;
import cn.biq.mn.validation.TimeField;
import cn.biq.mn.validation.TitleField;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class NoteDayUpdateForm {

    @TitleField
    private String title;

    @NotesField
    private String notes;

    @TimeField
    private Long startDate;

    @TimeField
    private Long endDate;

    private Integer repeatType;

    private Integer interval;

}
