package com.nextify.blog.config;

import com.nextify.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 请求拦截器
 * 继承 OncePerRequestFilter 确保每个请求只被拦截一次
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // 1. 从 Header 中获取 Authorization
        String token = request.getHeader("Authorization");

        // 2. 如果 Header 为空，直接进入下一个过滤器（如果是受限接口，Security 后面会拦住）
        if (!StringUtils.hasText(token)) {
            chain.doFilter(request, response);
            return;
        }

        // 3. 解析并验证 Token
        Claims claims = jwtUtils.getClaimsByToken(token);
        if (claims == null || jwtUtils.isTokenExpired(claims)) {
            // Token 无效或过期，不设置上下文，直接放行
            chain.doFilter(request, response);
            return;
        }

        // 4. 获取用户名并构建 Security 认证对象
        String username = claims.getSubject();

        // 注意：这里的权限列表暂时传空 (new ArrayList<>() )，后续复杂权限可以从数据库查
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>());

        // 5. 将认证信息存入全局上下文，Security 就会认为当前用户已登录
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 6. 继续后续过滤链
        chain.doFilter(request, response);
    }
}