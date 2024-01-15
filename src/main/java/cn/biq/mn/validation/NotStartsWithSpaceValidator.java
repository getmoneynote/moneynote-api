package cn.biq.mn.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;


public class NotStartsWithSpaceValidator implements ConstraintValidator<NotStartsWithSpace, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!StringUtils.hasText(value)) return true;
        return !value.startsWith(" ");
    }

}
