package com.moneynote.setup;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * setup 模式(未配置数据库时)专用:
 * 除 /setup 本身、静态资源之外的所有请求,一律 302 到 /setup。
 *
 * 正式运行的 SpaForwardFilter 在 com.moneynote.config 包,
 * 不会被 SetupApplication(scanBasePackages = "com.moneynote.setup")扫描到,
 * 所以这里需要一份 setup 模式的等价逻辑。
 *
 * 注意:必须用 @ConditionalOnProperty 限制只在 setup 模式注册。
 * 否则正式模式下 Application 扫描 com.moneynote 全包时会把本 filter 也注册,
 * 导致所有非 /setup 请求被 302 到 /setup,而 /setup 又因已配置 redirect:/,
 * 两个 302 互相循环。
 */
@Component
@ConditionalOnProperty(name = "app.db.configured", havingValue = "false")
public class SetupRedirectFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean isSetup = uri.startsWith("/setup");
        boolean isStaticFile = uri.contains(".");   // logo.svg / index.html 等静态资源直接放行
        if (!isSetup && !isStaticFile && !uri.equals("/favicon.ico")) {
            response.sendRedirect(request.getContextPath() + "/setup");
            return;
        }
        chain.doFilter(request, response);
    }
}
