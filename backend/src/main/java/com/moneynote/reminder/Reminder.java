package com.moneynote.reminder;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.base.BaseEntity;
import com.moneynote.user.User;

@Entity
@Table(name = "t_reminder")
@Getter @Setter
public class Reminder extends BaseEntity {

    @ManyToOne(
            optional = false,
            fetch = FetchType.LAZY
    )
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /** 提醒标题 */
    @Column(length = 64, nullable = false)
    private String title;

    /** 备注 */
    @Column(length = 1024)
    private String notes;

    /** 执行日期时间戳（毫秒） */
    @Column(nullable = false)
    private Long executeDate;

    /** 是否已完成 */
    @Column(nullable = false)
    private Boolean completed = false;

}
