package cn.biq.mn.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import cn.biq.mn.base.response.SimpleResponse;
import cn.biq.mn.base.utils.Constant;
import cn.biq.mn.base.utils.MessageSourceUtil;
import cn.biq.mn.base.utils.WebUtils;


@Component
@RequiredArgsConstructor
public class MyAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final MessageSourceUtil messageSourceUtil;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) {
        WebUtils.response(response, new SimpleResponse(
                false,
                HttpServletResponse.SC_FORBIDDEN,
                messageSourceUtil.getMessage("admin.authentication.invalid"),
                Constant.SHOW_TYPE_ERROR_MESSAGE
        ));
    }

}
