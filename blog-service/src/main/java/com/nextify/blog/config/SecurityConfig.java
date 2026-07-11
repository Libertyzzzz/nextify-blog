package com.nextify.blog.config;

import com.nextify.blog.config.security.CustomAccessDeniedHandler;
import com.nextify.blog.config.security.CustomAuthenticationEntryPoint;
import com.nextify.blog.config.security.PublicApiRequestMatcher; // 导入 PublicApiRequestMatcher
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置
 * 已集成 JWT 过滤器
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private PublicApiRequestMatcher publicApiRequestMatcher;

    @Autowired
    private CustomAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                // 允许跨域
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 因为使用 JWT，所以不需要 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 允许所有带有 @PublicApi 注解的接口无需认证
                        // 允许访问静态资源，例如上传的图片等
                        .requestMatchers("/uploads/**").permitAll()

                        .requestMatchers(publicApiRequestMatcher).permitAll()

                        .anyRequest().authenticated()
                )
                // 注册自定义异常处理器
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        // 将我们的 JWT 过滤器放在账号密码过滤器之前
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 跨域配置的具体内容
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 允许前端的来源（注意：不要写 *，联调时写具体的地址）
        // configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173", "http://127.0.0.1:5173"));

        // 配置前端来源
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173",
                "https://nextify.cn",     // 必须包含你的域名
                "http://nextify.cn"
        ));
        // 允许的请求方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        // 允许的 Header
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        // 是否允许携带 Cookie (如果用 JWT，通常可以设为 true 也可以不设)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
