package cn.biq.mn.exception;

import cn.biq.mn.response.*;
import cn.biq.mn.utils.MessageSourceUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


// https://www.baeldung.com/rest-api-error-handling-best-practices
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSourceUtil messageSourceUtil;

    // 输入验证没通过 post的body
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(MethodArgumentNotValidException e) {
        e.printStackTrace();
        ValidationErrorResponse response = new ValidationErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), messageSourceUtil.getMessage("valid.fail"));
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            response.getViolations().add(new Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }
        return response;
    }

    // 输入验证没通过 query parameters. variables within the path
    // 实体类上面标注的注解验证
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    ValidationErrorResponse exceptionHandler(ConstraintViolationException e) {
        e.printStackTrace();
        ValidationErrorResponse response = new ValidationErrorResponse(4221, messageSourceUtil.getMessage("valid.fail"));
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            response.getViolations().add(new Violation(violation.getPropertyPath().toString(), violation.getMessage()));
        }
        return response;
    }

    // get方法，query parameters 验证没通过
    @ExceptionHandler(BindException.class)
    @ResponseBody
    ValidationErrorResponse exceptionHandler(BindException e) {
        e.printStackTrace();
        ValidationErrorResponse response = new ValidationErrorResponse(4223, messageSourceUtil.getMessage("valid.fail"));
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            response.getViolations().add(new Violation(error.getField(), error.getDefaultMessage()));
        }
        return response;
    }

    // entity 类上面的验证
    // https://stackoverflow.com/questions/45070642/springboot-doesnt-handle-org-hibernate-exception-constraintviolationexception
    // https://stackoverflow.com/questions/40808319/hibernate-validation-results-in-spring-transactionsystemexception
//    @ExceptionHandler(TransactionSystemException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public BaseResponse exceptionHandler(TransactionSystemException e) {
        e.printStackTrace();
        if (e.getRootCause() instanceof ConstraintViolationException) {
            ConstraintViolationException constraintViolationException = (ConstraintViolationException) e.getRootCause();
            ValidationErrorResponse response = new ValidationErrorResponse(4222, messageSourceUtil.getMessage("valid.fail"));
            for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
                response.getViolations().add(new Violation(violation.getPropertyPath().toString(), violation.getMessage()));
            }
            return response;
        } else {
            // TODO
            e.printStackTrace();
            return new SimpleResponse(false, HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), e.getMessage());
        }
    }

    // 最后一层database的验证，长度，unique约束等
    // https://stackoverflow.com/questions/2109476/how-to-handle-dataintegrityviolationexception-in-spring
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(DataIntegrityViolationException e) {
        e.printStackTrace();
        String rootMsg = "";
        if (e.getRootCause() != null) {
            rootMsg = e.getRootCause().getMessage();
        }
        return new SimpleResponse(false, HttpStatus.CONFLICT.value(), rootMsg);
    }

    @ExceptionHandler(ItemExistsException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(ItemExistsException e) {
        e.printStackTrace();
        String errMsg;
        if (StringUtils.hasText(e.getMessage())) {
            errMsg = messageSourceUtil.getMessage(e.getMessage());
        } else {
            errMsg = messageSourceUtil.getMessage("item.exists.exception");
        }
        return new SimpleResponse(false, HttpStatus.CONFLICT.value(), errMsg);
    }

    @ExceptionHandler(ItemNotFoundException.class)
    @ResponseBody  // 404
    public BaseResponse exceptionHandler(ItemNotFoundException e) {
        e.printStackTrace();
        String errMsg;
        if (StringUtils.hasText(e.getMessage())) {
            errMsg = messageSourceUtil.getMessage(e.getMessage());
        } else {
            errMsg = messageSourceUtil.getMessage("item.not.found.exception");
        }
        return new SimpleResponse(false, HttpStatus.NOT_FOUND.value(), errMsg);
    }

    @ExceptionHandler(FailureMessageException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(FailureMessageException e) {
        e.printStackTrace();
        return new SimpleResponse(false, messageSourceUtil.getMessage(e.getMessage()), e.getShowType());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public BaseResponse exceptionHandler(HttpMessageNotReadableException e) {
        e.printStackTrace();
        return new SimpleResponse(false, messageSourceUtil.getMessage(e.getClass().getSimpleName()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public BaseResponse exceptionHandler(Exception e) {
        e.printStackTrace();
        return new SimpleResponse(false, messageSourceUtil.getMessage("DefaultException"));
    }

}
