package cn.biq.mn.admin.interceptor;

import cn.biq.mn.admin.rbac.admin.Admin;
import cn.biq.mn.admin.rbac.admin.AdminRepository;
import cn.biq.mn.admin.security.CurrentSession;
import cn.biq.mn.admin.security.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import cn.biq.mn.base.exception.FailureMessageException;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.utils.WebUtils;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final CurrentSession currentSession;
    private final AdminRepository adminRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = WebUtils.resolveToken(request);
        if (!StringUtils.hasText(token) && currentSession.getAdmin() == null) {
            throw new FailureMessageException("admin.authentication.empty");
        }
        if (StringUtils.hasText(token) && !token.equals(currentSession.getAccessToken())) {
            try {
                Admin admin = adminRepository.findById(jwtUtils.getUserId(token)).orElseThrow(ItemNotFoundException::new);
                currentSession.setAccessToken(token);
                currentSession.setAdmin(admin);
            } catch (Exception e) {
                throw new FailureMessageException("admin.authentication.invalid");
            }
        }
        return true;
    }
}
