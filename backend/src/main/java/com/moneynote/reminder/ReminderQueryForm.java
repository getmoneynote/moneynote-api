package com.moneynote.reminder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReminderQueryForm {

    /**
     * 完成状态筛选：null-全部 false-未完成 true-已完成
     */
    private Boolean completed;

}
