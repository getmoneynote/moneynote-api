package cn.biq.mn.base.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Digits;

import java.lang.annotation.*;


// 所有与金额有关的，包括账户余额，账单金额等。
@Digits(integer = 9, fraction = 2)

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface AmountField {
    
    String message() default "{valid.fail}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
    
}
