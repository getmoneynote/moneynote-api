package com.moneynote.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SpaForwardFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        boolean isApi = uri.startsWith("/api/");
        boolean isSetup = uri.startsWith("/setup");
        boolean isH2Console = uri.startsWith("/h2-console");   // H2 控制台 servlet 路径，别转发成 index.html
        boolean isStaticFile = uri.contains(".");   // js/css/png 等带后缀的直接放行
        boolean isGet = "GET".equalsIgnoreCase(request.getMethod());

        // 已配置完成：正常走 SPA 转发逻辑，同时把 /setup、/h2-console 排除掉
        // （已配置状态下理论上不会再访问 /setup，但防御性地排除，避免误转发成 index.html）
        if (!isApi && !isSetup && !isH2Console && !isStaticFile && isGet) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }
}
