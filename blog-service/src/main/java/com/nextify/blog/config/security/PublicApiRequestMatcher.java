package com.nextify.blog.config.security;

import com.nextify.blog.common.annotaion.PublicApi;
import jakarta.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class PublicApiRequestMatcher implements RequestMatcher {

    private final AntPathMatcher antPathMatcher = new AntPathMatcher();
    private final Set<String> publicUrls = new HashSet<>();
    private final AtomicBoolean initialized = new AtomicBoolean(false);

    // 使用 @Lazy 注入映射处理器，防止 SecurityConfig 启动时的循环依赖
    @Autowired
    @Lazy
    private RequestMappingHandlerMapping handlerMapping;

    /**
     * 匹配逻辑
     */
    @Override
    public boolean matches(HttpServletRequest request) {
        log.debug("Security checking path: {}", request.getServletPath());
        
        // 初始化扫描（仅一次）
        if (!initialized.get()) {
            synchronized (publicUrls) {
                if (initialized.compareAndSet(false, true)) {
                    log.info("触发首次公开接口扫描...");
                    scanPublicApis();
                }
            }
        }

        String path = request.getServletPath();
        boolean match = publicUrls.stream().anyMatch(url -> antPathMatcher.match(url, path));
        if (match) { log.debug("Path [{}] matches PublicApi", path); }
        return match;
    }

    private void scanPublicApis() {
        log.info("准备获取所有映射处理器...");
        Map<RequestMappingInfo, HandlerMethod> handlerMethods = handlerMapping.getHandlerMethods();
        log.info("已获取映射, 数量: {}", handlerMethods.size());

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            
            // 1. 优先使用 HandlerMethod 自身的方法，更准确
            PublicApi methodAnnotation = handlerMethod.getMethodAnnotation(PublicApi.class);
            // 2. 检查类上是否有注解
            PublicApi classAnnotation = AnnotationUtils.findAnnotation(handlerMethod.getBeanType(), PublicApi.class);

            if (methodAnnotation != null || classAnnotation != null) {
                // 3. Spring Boot 3 / Spring 6 推荐使用 getPatternValues() 获取路径
                Set<String> patterns = entry.getKey().getPatternValues();
                if (patterns != null && !patterns.isEmpty()) {
                    log.info("发现公开接口: {} -> {}", handlerMethod.getMethod().getName(), patterns);
                    publicUrls.addAll(patterns);
                }
            }
        }
        log.info("公开接口扫描完成，共放行路径: {} 个", publicUrls.size());
    }
}