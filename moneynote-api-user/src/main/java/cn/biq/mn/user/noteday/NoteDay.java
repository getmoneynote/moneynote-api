package cn.biq.mn.user.noteday;

import cn.biq.mn.user.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.validation.NotesField;
import cn.biq.mn.base.validation.TimeField;
import cn.biq.mn.base.validation.TitleField;
import cn.biq.mn.base.base.BaseEntity;

@Entity
@Table(name = "t_user_note_day")
@Getter @Setter
public class NoteDay extends BaseEntity {

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @NotNull
    private User user;

    @Column(length = 64, nullable = false)
    @NotNull
    @TitleField
    private String title;

    @Column(length = 4096)
    @NotesField
    private String notes; //备注

    @Column(nullable = false)
    @NotNull
    @TimeField
    private Long startDate; //起始日期

    @Column
    @NotNull
    @TimeField
    private Long endDate; //结束日期

    @Column
    @TimeField
    private Long nextDate; //下次执行日期

    @Column
    @NotNull
    private Integer repeatType; //0单次 1每天，2每月 3每年

    @Column(name = "c_interval")
    private Integer interval; //间隔

    //总执行次数
    @NotNull
    private Integer totalCount;

    //已执行次数
    @NotNull
    private Integer runCount;

}
