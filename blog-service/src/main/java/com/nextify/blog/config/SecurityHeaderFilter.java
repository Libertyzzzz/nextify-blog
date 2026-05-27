package com.nextify.blog.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

// @Component
public class SecurityHeaderFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 内容安全策略（防止XSS和数据注入）
        httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self'; " +
                        "connect-src 'self' https:;");

        // 防止点击劫持
        httpResponse.setHeader("X-Frame-Options", "DENY");

        // 防止MIME类型混淆攻击
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");

        // 启用XSS保护
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

        // 禁止浏览器缓存敏感数据
        httpResponse.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        httpResponse.setHeader("Pragma", "no-cache");
        httpResponse.setHeader("Expires", "0");


        // 安全传输（HTTPS环境）
        // httpResponse.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        chain.doFilter(request, response);
    }
}
