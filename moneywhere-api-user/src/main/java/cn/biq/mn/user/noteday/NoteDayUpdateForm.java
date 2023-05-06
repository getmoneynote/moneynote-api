package cn.biq.mn.user.noteday;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.TitleField;

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
