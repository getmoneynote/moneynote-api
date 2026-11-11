package com.moneynote.user;

import jakarta.validation.Constraint;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Size(min = 1, max = 16, message = "{validation.user.username.size}")
@Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "{validation.user.username.pattern}")
// @ReportAsSingleViolation
@Documented
@Constraint(validatedBy = {}) // 空数组:告诉 Bean Validation 这是组合约束,不需要自己写 Validator
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUsername {

    String message() default "{validation.user.username.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
