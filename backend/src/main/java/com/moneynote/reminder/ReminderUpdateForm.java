package com.moneynote.reminder;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import com.moneynote.validate.ValidNotes;

@Getter @Setter
public class ReminderUpdateForm {

    /**
     * 部分更新语义：null 表示不修改；传了就必须合法（非空白且不超长）。
     * @Pattern 对 null 放行，正好表达"传了才校验非空白"。
     */
    @Size(max = 64)
    @Pattern(regexp = ".*\\S.*", message = "{jakarta.validation.constraints.NotBlank.message}")
    private String title;

    @ValidNotes
    private String notes;

    /**
     * Unix 毫秒时间戳。业务上仅表示"日期"（前端固定取当天 00:00），此处只做
     * 基本范围校验：>= 0 且不晚于 3000-01-01，拦截异常/恶意取值。
     * 完整的业务规则校验由 Service 层负责。
     */
    @Min(value = 0, message = "{validation.timestamp.invalid}")
    @Max(value = 32503680000000L, message = "{validation.timestamp.invalid}") // 3000-01-01T00:00:00Z
    private Long executeDate;

    private Boolean completed;

}
