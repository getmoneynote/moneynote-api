package cn.biq.mn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotZeroValidator.class)
@Documented
public @interface NotZero {

    String message() default "{custom.validation.constraints.NotZero.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
