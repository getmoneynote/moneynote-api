package cn.biq.mn.user.interceptor;

import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookRepository;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.group.GroupRepository;
import cn.biq.mn.user.security.CurrentSession;
import cn.biq.mn.user.security.JwtUtils;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.user.UserRepository;
import com.auth0.jwt.exceptions.JWTVerificationException;
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
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final GroupRepository groupRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = WebUtils.resolveToken(request);
        if (!StringUtils.hasText(token)) {
            token = (String) request.getSession().getAttribute("accessToken");
        }
        if (!StringUtils.hasText(token) && currentSession.getUser() == null) {
            throw new FailureMessageException("user.authentication.empty");
        }
        if (StringUtils.hasText(token) && !token.equals(currentSession.getAccessToken())) {
            try {
                User user = userRepository.findById(jwtUtils.getUserId(token)).orElseThrow(() -> new FailureMessageException("user.authentication.invalid"));
                // 必须手动获取，不然报 org.hibernate.LazyInitializationException
                Book book = null;
                if (user.getDefaultBook() != null) {
                    book = bookRepository.findById(user.getDefaultBook().getId()).orElseThrow(ItemNotFoundException::new);
                }
                Group group = groupRepository.findById(user.getDefaultGroup().getId()).orElseThrow(ItemNotFoundException::new);
                currentSession.setAccessToken(token);
                currentSession.setUser(user);
                currentSession.setBook(book);
                currentSession.setGroup(group);
            } catch (JWTVerificationException e) {
                throw new FailureMessageException("user.authentication.invalid");
            }
        }
        return true;
    }
}
