package cn.biq.mn.admin.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import cn.biq.mn.base.response.BaseResponse;
import cn.biq.mn.base.response.SimpleResponse;
import cn.biq.mn.base.utils.Constant;
import cn.biq.mn.base.utils.MessageSourceUtil;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class SecurityExceptionHandler {

    private final MessageSourceUtil messageSourceUtil;

    @ExceptionHandler(value = BadCredentialsException.class)
    @ResponseBody
    public BaseResponse handleException(BadCredentialsException e) {
        return new SimpleResponse(false, messageSourceUtil.getMessage("admin.login.wrong.credentials"), Constant.SHOW_TYPE_ERROR_MESSAGE);
    }

}
