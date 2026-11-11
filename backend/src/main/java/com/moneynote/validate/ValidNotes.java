package com.moneynote.validate;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@Size(max = 1024)

@Documented
@Constraint(validatedBy = {}) // 空数组:告诉 Bean Validation 这是组合约束,不需要自己写 Validator
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNotes {

    String message() default "{error.validation.failed}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
