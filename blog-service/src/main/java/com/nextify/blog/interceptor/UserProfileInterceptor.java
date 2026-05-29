package com.nextify.blog.interceptor;

import com.nextify.blog.service.AnonymousUserService;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户画像拦截器
 */
@Component
@Slf4j
public class UserProfileInterceptor implements HandlerInterceptor {

    @Resource
    private AnonymousUserService anonymousUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 仅处理 GET 请求（浏览文章、首页等）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            String ip = IPUtils.getRealIp(request);
            String userAgent = request.getHeader("User-Agent");
            log.info("用户画像拦截器：IP={}, User-Agent={}", ip, userAgent);
            
            // 异步处理画像逻辑，不阻塞主流程
           anonymousUserService.processAnonymousUser(ip, userAgent);
        }
        return true;
    }


}