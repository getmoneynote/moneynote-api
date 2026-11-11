package com.moneynote.config;

import com.moneynote.exception.UnauthorizedException;
import com.moneynote.user.CurrentUserHolder;
import com.moneynote.utils.CacheHolder;
import com.moneynote.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = WebUtils.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            throw new UnauthorizedException();
        }
        Long userId = CacheHolder.token.get(token);
        if (userId == null) {
            throw new UnauthorizedException("error.auth.token.invalid");
        }
        CurrentUserHolder.set(userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        CurrentUserHolder.clear();
    }
}
