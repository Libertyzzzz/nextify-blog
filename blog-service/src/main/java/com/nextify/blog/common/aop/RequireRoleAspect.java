package com.nextify.blog.common.aop;

import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.annotaion.RequireRole;
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * 角色校验 AOP 切面
 * 拦截 @RequireRole 注解的方法，进行角色校验
 */
@Slf4j
@Aspect
@Component
public class RequireRoleAspect {

    @Autowired
    private PermissionService permissionService;

    @Around("@annotation(com.nextify.blog.common.annotaion.RequireRole)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole annotation = method.getAnnotation(RequireRole.class);

        String[] requiredRoles = annotation.value();
        if (requiredRoles == null || requiredRoles.length == 0) {
            return joinPoint.proceed();
        }

        // 获取当前用户ID
        Long userId = UserContextHolder.getUserId();
        if (userId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }

        // 获取用户权限信息
        UserPermissionInfo permInfo = permissionService.getUserPermissionInfo(userId);

        // 超级管理员直接放行
        if (Boolean.TRUE.equals(permInfo.getIsSuperAdmin())) {
            log.debug("超级管理员 {}, 跳过角色校验", userId);
            return joinPoint.proceed();
        }

        Set<String> userRoles = permInfo.getRoleCodes();
        RequireRole.Logic logic = annotation.logic();

        boolean hasRole;
        if (logic == RequireRole.Logic.AND) {
            // AND 逻辑：必须拥有所有角色
            hasRole = true;
            for (String role : requiredRoles) {
                if (userRoles == null || !userRoles.contains(role)) {
                    hasRole = false;
                    break;
                }
            }
        } else {
            // OR 逻辑：任一角色即可
            hasRole = false;
            for (String role : requiredRoles) {
                if (userRoles != null && userRoles.contains(role)) {
                    hasRole = true;
                    break;
                }
            }
        }

        if (!hasRole) {
            log.warn("用户 {} 角色校验失败, 需要: {}, 逻辑: {}",
                userId, String.join(",", requiredRoles), logic);
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return joinPoint.proceed();
    }
}
