package cn.biq.mn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotStartsWithSpaceValidator.class)
@Documented
public @interface NotStartsWithSpace {

    String message() default "{custom.validation.constraints.NotStartsWithSpace.message}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}
