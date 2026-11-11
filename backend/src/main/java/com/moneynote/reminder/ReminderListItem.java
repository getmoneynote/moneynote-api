package com.moneynote.reminder;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ReminderListItem {

    private Long id;
    private String title;
    private String notes;
    private Long executeDate;
    private Boolean completed;

}
