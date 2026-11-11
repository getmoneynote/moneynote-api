package com.moneynote.user;


import com.moneynote.book.Book;
import com.moneynote.book.BookRepository;
import com.moneynote.currency.Currency;
import com.moneynote.currency.CurrencyRepository;
import com.moneynote.exception.BusinessException;
import com.moneynote.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CurrencyRepository currencyRepository;
    private final BookRepository bookRepository;

    @Value("${invite_code:111111}")
    private String inviteCode;

    @Transactional
    // 一个简单的注册在高并发下都会出问题，可以展开讲很多东西
    public void register(RegisterForm form) {
        if (StringUtils.hasText(inviteCode) && !inviteCode.equals(form.getInviteCode())) {
            throw new BusinessException("error.user.register.invite.wrong");
        }
        if (userRepository.existsByUsername(form.getUsername())) {
            throw new BusinessException("error.user.register.conflict");
        }
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());//私有部署，不考虑密码安全问题
        userRepository.save(user);
        initData(user);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String login(LoginForm form) {
        User user = userRepository.findOneByUsername(form.getUsername()).orElse(null);
        if (user == null || !user.getPassword().equals(form.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return SessionManager.createSession(user);
    }

    /**
     * 退出登录：只作废当前 token 对应的会话（纯内存操作，不触库）。
     * token 无效时静默返回——前端无论调用成败都会清除本地登录态。
     */
    public void logout(String token) {
        SessionManager.invalidateSession(token);
    }

    public InitStateResponse getInitState() {
        InitStateResponse response = new InitStateResponse();
        Long currentUserId = CurrentUserHolder.get();
        SessionInfo sessionInfo = SessionManager.getSessionInfo(currentUserId);
        User user = sessionInfo.getCurrentUser();
        response.setUser(buildUserSessionVo(user));
        return response;
    }

    /**
     * 用户会话 VO 的唯一构建出口：getInitState / setBaseCurrency / LlmConfigService.activate 共用，
     * 保证各处返回的 VO 永远同构（含 decimalPlaces 补齐逻辑）。
     * 本位币被删悬空时 ifPresent 忽略，decimalPlaces 留空由前端兜底。
     */
    public UserSessionVo buildUserSessionVo(User user) {
        UserSessionVo vo = UserConverter.toVo(user);
        // 补齐本位币小数位：前端新增账户默认选中本位币，先拿到 decimalPlaces 可避免
        // 等 currency options 加载完成前 precision 短暂跳变
        if (user.getBaseCurrencyId() != null) {
            currencyRepository.findById(user.getBaseCurrencyId())
                    .ifPresent(c -> vo.setBaseCurrencyDecimalPlaces(c.getDecimalPlaces()));
        }
        return vo;
    }

    @Transactional
    public UserSessionVo setBaseCurrency(Long id) {
        Long userId = CurrentUserHolder.get();
        Currency currency = currencyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("resource.currency"));
        User user = SessionManager.getCurrentUser();
        user.setBaseCurrencyId(currency.getId());
        userRepository.save(user);
        // 返回变更后的会话 VO：响应即失效通知，前端据此直接更新全局副本，无需二次拉取
        return buildUserSessionVo(user);
    }

    private void initData(User user) {
        Currency usd = new Currency();
        usd.setUser(user);
        usd.setCode("USD");
        usd.setName("美元");
        usd.setSymbol("$");
        usd.setDecimalPlaces(2);
        usd.setUsdRate(BigDecimal.ONE);
        currencyRepository.save(usd);
        Currency cny = new Currency();
        cny.setUser(user);
        cny.setCode("CNY");
        cny.setName("人民币");
        cny.setSymbol("￥");
        cny.setDecimalPlaces(2);
        cny.setUsdRate(BigDecimal.valueOf(6.8));
        currencyRepository.save(cny);
        user.setBaseCurrencyId(cny.getId());

        Book book = new Book();
        book.setUser(user);
        book.setName("生活账本");
        book.setCurrency(cny);
        bookRepository.save(book);
        user.setCurrentBookId(book.getId());

        userRepository.save(user);

    }

}
