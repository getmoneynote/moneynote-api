package com.moneynote.user;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Size(min = 6, max = 32, message = "{validation.user.password.size}")

@Documented
@Constraint(validatedBy = {}) // 空数组:告诉 Bean Validation 这是组合约束,不需要自己写 Validator
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "{validation.user.password.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
