package cn.biq.mn.base.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;


public class NotEndsWithSpaceValidator implements ConstraintValidator<NotEndsWithSpace, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) return true;
        return !value.endsWith(" ");
    }

}
