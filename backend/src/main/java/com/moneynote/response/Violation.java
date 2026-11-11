package com.moneynote.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 单个字段的校验违规信息，描述"哪个字段、因为什么校验失败、被拒绝的值是什么"。
 *
 * <p>由 {@link ValidationErrorResponse#getViolations()} 聚合，一次校验失败
 * 会为每个违规字段生成一个 {@link Violation}。
 *
 * <p>三个字段对应关系：
 * <ul>
 *   <li>{@link #fieldName}：校验失败的字段名，前端据此定位输入框；</li>
 *   <li>{@link #message}：该字段的具体错误文案；</li>
 *   <li>{@link #rejectedValue}：用户提交的、未通过校验的原始值（便于回显或排查）。</li>
 * </ul>
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Violation {

    private String fieldName;

    private String message;

    private Object rejectedValue;

}
