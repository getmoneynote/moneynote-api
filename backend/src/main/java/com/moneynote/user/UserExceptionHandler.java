package com.moneynote.user;

import com.moneynote.response.MessageResponse;
import com.moneynote.response.SimpleResponse;
import com.moneynote.utils.MessageResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * user 包内部的异常处理，只作用于 {@link UserController}。
 *
 * <p>{@link InvalidCredentialsException} 在这里处理，而不是放进全局
 * {@code GlobalExceptionHandler}：它是 user 模块专属的业务异常，
 * 由 user 包自己维护，避免全局 handler 越积越多。
 *
 * <p>通过 {@code assignableTypes} 把本 advice 限定到 {@link UserController}，
 * 保证它不会拦截到其它 Controller 抛出的异常。
 */
@RestControllerAdvice(assignableTypes = UserController.class)
@RequiredArgsConstructor
public class UserExceptionHandler {

    private final MessageResolver messageResolver;

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED) // 401 用户名或密码错误
    public SimpleResponse exceptionHandler(InvalidCredentialsException e) {
        return new MessageResponse(false, messageResolver.resolve(e.getMessage(), e.getArgs()));
    }

}
