package com.nextify.blog.interceptor;

import com.nextify.blog.service.AnonymousUserService;
import com.nextify.blog.utils.IPUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
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

    private static final String PROCESSED_FLAG = "USER_PROFILE_PROCESSED";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 仅处理 GET 请求（浏览文章、首页等）
        if ("GET".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession();
            
            // 如果当前会话已经处理过画像，直接放行，不再执行后续逻辑
            if (session.getAttribute(PROCESSED_FLAG) != null) {
                return true;
            }

            String ip = IPUtils.getRealIp(request);
            String userAgent = request.getHeader("User-Agent");
            
            try {
                // 异步处理画像逻辑，不阻塞主流程
                anonymousUserService.processAnonymousUser(ip, userAgent);
                // 执行成功后，在 Session 中存入标记
                session.setAttribute(PROCESSED_FLAG, true);
            } catch (Exception e) {
                log.error("User Profile: {}", e.getMessage());
            }
        }
        return true;
    }


}