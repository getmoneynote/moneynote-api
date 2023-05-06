package cn.biq.mn.user.security;

import cn.biq.mn.user.book.Book;
import cn.biq.mn.user.book.BookRepository;
import cn.biq.mn.user.group.Group;
import cn.biq.mn.user.group.GroupRepository;
import cn.biq.mn.user.user.User;
import cn.biq.mn.user.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import cn.biq.mn.base.exception.ItemNotFoundException;
import cn.biq.mn.base.response.SimpleResponse;
import cn.biq.mn.base.utils.Constant;
import cn.biq.mn.base.utils.MessageSourceUtil;
import cn.biq.mn.base.utils.WebUtils;

import java.io.IOException;

// https://stackoverflow.com/questions/41975045/how-to-design-a-good-jwt-authentication-filter
@RequiredArgsConstructor
//@Component //已弃用
public class AuthTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CurrentSession currentSession;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final GroupRepository groupRepository;
    private final MessageSourceUtil messageSourceUtil;

    @Override
//    @Transactional 加这个注释就报错
    // https://stackoverflow.com/questions/49339820/spring-boot-unable-to-proxy-interface-implementing-method-warn-message
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!(request.getRequestURI().contains("login") || request.getRequestURI().contains("register"))) {
            try {
                String token = WebUtils.resolveToken(request);
                if (StringUtils.hasText(token) && !token.equals(currentSession.getAccessToken())) {
                    User user = userRepository.findById(jwtUtils.getUserId(token)).orElseThrow(ItemNotFoundException::new);
                    // 必须手动获取，不然报 org.hibernate.LazyInitializationException
                    Book book = bookRepository.findById(user.getDefaultBook().getId()).orElseThrow(ItemNotFoundException::new);
                    Group group = groupRepository.findById(user.getDefaultGroup().getId()).orElseThrow(ItemNotFoundException::new);
                    currentSession.setAccessToken(token);
                    currentSession.setUser(user);
                    currentSession.setBook(book);
                    currentSession.setGroup(group);
                }
                filterChain.doFilter(request, response);
            } catch (Exception e) {
                WebUtils.response(response, new SimpleResponse(
                        false,
                        HttpServletResponse.SC_FORBIDDEN,
                        messageSourceUtil.getMessage("user.authentication.invalid"),
                        Constant.SHOW_TYPE_ERROR_MESSAGE
                ));
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }

}
