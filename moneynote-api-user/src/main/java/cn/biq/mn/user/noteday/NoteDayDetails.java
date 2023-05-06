package cn.biq.mn.user.noteday;

import lombok.Getter;
import lombok.Setter;
import cn.biq.mn.base.base.BaseDetails;

@Getter @Setter
public class NoteDayDetails extends BaseDetails {

    private String title;
    private String notes;
    private Long startDate;
    private Long endDate;
    private Integer repeatType;
    private Integer interval;
    private String repeatDescription;
    private Long countDown;//倒计时天数
    //下次执行日期
    private Long nextDate;
    //总执行次数
    private Integer totalCount;
    //已执行次数
    private Integer runCount;
    //剩余次数
    private Integer remainCount;

}
