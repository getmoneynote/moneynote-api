package cn.biq.mn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotEndsWithSpaceValidator.class)
@Documented
public @interface NotEndsWithSpace {

    String message() default "{custom.validation.constraints.NotEndsWithSpace.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
