package com.moneynote.user;

import com.moneynote.response.SimpleResponse;
import com.moneynote.response.DataResponse;
import com.moneynote.utils.WebUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @RequestMapping(method = RequestMethod.POST, value = "/register")
    public SimpleResponse handleRegister(@Valid @RequestBody RegisterForm form) {
        userService.register(form);
        return new SimpleResponse(true);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/login")
    public SimpleResponse handleLogin(@Valid @RequestBody LoginForm form) {
        String token = userService.login(form);
        return new DataResponse<>(new LoginSuccessVo(token));
    }

    @RequestMapping(method = RequestMethod.POST, value = "/logout")
    public SimpleResponse handleLogout(HttpServletRequest request) {
        // 作废当前请求携带的 token；本接口走 AuthInterceptor，进来即 token 有效
        userService.logout(WebUtils.resolveToken(request));
        return new SimpleResponse(true);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/initState")
    public SimpleResponse handleInitState() {
        return new DataResponse<>(userService.getInitState());
    }

    @RequestMapping(method = RequestMethod.PATCH, value = "/setBaseCurrency/{id:\\d+}")
    public SimpleResponse handleSetBaseCurrency(@PathVariable("id") Long id) {
        // 返回变更后的会话 VO，响应即失效通知：前端直接用它更新全局用户态
        return new DataResponse<>(userService.setBaseCurrency(id));
    }
}
