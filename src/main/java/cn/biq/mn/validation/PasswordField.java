package cn.biq.mn.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

@NotBlank
@Size(min = 6, max = 32)
@Pattern(regexp = "^[A-Za-z0-9~`!@#\\$%\\^&\\*\\(\\)-_=\\+\\[\\]\\{\\}\\|]*$") //数字，字母，特殊字符(不包括空格)

@Documented
@Constraint(validatedBy = {})
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordField {

    String message() default "{valid.fail}";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

}

