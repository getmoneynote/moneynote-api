package com.moneynote.exception;

import com.moneynote.response.*;
import com.moneynote.utils.MessageResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageResolver messageResolver;

    // ---- BusinessException 及其子类 ----

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<SimpleResponse> exceptionHandler(BusinessException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs())));
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401
    public SimpleResponse exceptionHandler(UnauthorizedException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    @ExceptionHandler(ResourceDuplicateException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public SimpleResponse exceptionHandler(ResourceDuplicateException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    @ExceptionHandler(ResourceInvalidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public SimpleResponse exceptionHandler(ResourceInvalidException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public SimpleResponse exceptionHandler(ResourceNotFoundException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    @ExceptionHandler(ResourceProtectedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN) // 403
    public SimpleResponse exceptionHandler(ResourceProtectedException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    @ExceptionHandler(ResourceReferencedException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public SimpleResponse exceptionHandler(ResourceReferencedException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

    // ---- 框架级异常 ----

    // 场景1: @RequestBody + @Valid 校验失败
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public SimpleResponse exceptionHandler(MethodArgumentNotValidException e) {
        ValidationErrorResponse response = new ValidationErrorResponse(messageResolver.resolve("error.validation.failed"));
        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            response.getViolations().add(
                    new Violation(
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            fieldError.getRejectedValue()
                    )
            );
        }
        return response;
    }

    // 场景2: 请求路径未匹配到任何 Controller（对应 error.route.not.found）
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public SimpleResponse exceptionHandler(NoResourceFoundException e) {
        return new MessageResponse(false, messageResolver.resolve("error.route.not.found"));
    }

    // ---- 兜底：未知系统异常 ----

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT) // 409
    public SimpleResponse exceptionHandler(DataIntegrityViolationException e) {
        // 外键约束违规（被引用无法删除），返回友好提示
        Throwable cause = e.getMostSpecificCause();
        String msg = cause != null ? cause.getMessage() : "";
        if (msg.contains("foreign key") || msg.contains("Referential integrity")
                || msg.contains("FOREIGN KEY") || msg.contains("referential constraint")) {
            return new MessageResponse(false, messageResolver.resolve("error.reference.constraint"));
        }
        log.warn("Data integrity violation", e);
        return new MessageResponse(false, messageResolver.resolve("error.data.integrity"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public SimpleResponse exceptionHandler(Exception e) {
        log.error("Unhandled exception", e);
        return new MessageResponse(false, messageResolver.resolve("error.unknown"));
    }

}