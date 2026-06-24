package com.nextify.blog.common.aop;

import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @RequiredLogin 注解的 AOP 实现
 * 用于在方法执行前检查用户是否已登录
 */
@Aspect
@Component
@Slf4j
public class RequiredLoginAspect {

    // 定义切点，匹配所有带有 @RequiredLogin 注解的方法
    @Pointcut("@annotation(com.nextify.blog.common.annotaion.RequiredLogin)")
    public void requiredLoginPointcut() {
    }

    @Around("requiredLoginPointcut()")
    public Object checkLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        log.debug("Entering @RequiredLogin aspect for method: {}", joinPoint.getSignature().toShortString());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查用户是否已认证
        // isAuthenticated() 为 true 且 principal 不是 "anonymousUser" 表示已登录
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            log.warn("Access denied: User not authenticated for method: {}", joinPoint.getSignature().toShortString());
            throw new BusinessException(ResultCode.UNAUTHORIZED); // 抛出未授权异常
        }

        log.debug("User authenticated. Proceeding with method: {}", joinPoint.getSignature().toShortString());
        // 用户已登录，继续执行目标方法
        return joinPoint.proceed();
    }
}
