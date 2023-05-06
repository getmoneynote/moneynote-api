package cn.biq.mn.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import cn.biq.mn.base.response.SimpleResponse;
import cn.biq.mn.base.utils.Constant;
import cn.biq.mn.base.utils.MessageSourceUtil;
import cn.biq.mn.base.utils.WebUtils;


@Component
@RequiredArgsConstructor
public class MyAccessDeniedHandler implements AccessDeniedHandler {

    private final MessageSourceUtil messageSourceUtil;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) {
        WebUtils.response(response, new SimpleResponse(
                false,
                HttpServletResponse.SC_FORBIDDEN,
                messageSourceUtil.getMessage("user.authentication.invalid"),
                Constant.SHOW_TYPE_ERROR_MESSAGE
        ));
    }

}
