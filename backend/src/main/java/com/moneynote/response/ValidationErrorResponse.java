package com.moneynote.response;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


/**
 * 参数校验失败（{@code @Valid} + {@code @RequestBody}）的响应，
 * 在 {@link MessageResponse} 基础上携带逐字段的违规明细。
 *
 * <p>{@code message} 为整体提示（如"参数校验失败"），
 * {@link #violations} 列表记录每个字段的具体错误，与 {@link Violation} 配合使用，
 * 前端可据此逐字段高亮并提示。参考
 * <a href="https://reflectoring.io/bean-validation-with-spring-boot/">reflectoring: Bean Validation</a>。
 */
// https://reflectoring.io/bean-validation-with-spring-boot/
@Getter @Setter
public class ValidationErrorResponse extends MessageResponse {

    private List<Violation> violations = new ArrayList<>();

    public ValidationErrorResponse(String message) {
        setSuccess(false);
        setMessage(message);
    }

}
