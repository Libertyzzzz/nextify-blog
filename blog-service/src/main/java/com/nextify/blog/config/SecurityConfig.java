package com.nextify.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置
 * 遵循 Apple 的"默认安全但易于扩展"原则
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (因为我们是前后端分离，使用 JWT，不需要 Session)
                .csrf(AbstractHttpConfigurer::disable)
                // 配置接口权限
                .authorizeHttpRequests(auth -> auth
                        // 允许所有以 /api/user/ 开头的 GET 请求 (用于你现在的测试)
                        .requestMatchers("/user/**").permitAll()
                        // 其他所有请求都需要认证 (后续我们会配置 JWT 过滤器)
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    /**
     * 密码加密器：使用标准 BCrypt 算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}