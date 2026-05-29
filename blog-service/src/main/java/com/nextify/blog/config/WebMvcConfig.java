package com.nextify.blog.config;

import com.nextify.blog.interceptor.UserProfileInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${nextify.upload.local-path:uploads/images}")
    private String uploadLocalPath;

    @Resource
    private UserProfileInterceptor userProfileInterceptor;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadLocalPath).toAbsolutePath().normalize();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }

    /**
     * 注册用户画像拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userProfileInterceptor)
            .addPathPatterns("/**")           // 拦截所有路径
            .excludePathPatterns(            // 排除不需要拦截的路径
                "/uploads/**",           // 静态资源
                "/error",                // 错误页面
                "/actuator/**"           // Spring Actuator（如果有）
            );
    }

    /**
     * 注册安全响应头过滤器
     */
    @Bean
    public FilterRegistrationBean<SecurityHeaderFilter> securityHeaderFilter() {
        FilterRegistrationBean<SecurityHeaderFilter> registration =
                new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeaderFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); // 优先级最高
        return registration;
    }



}
