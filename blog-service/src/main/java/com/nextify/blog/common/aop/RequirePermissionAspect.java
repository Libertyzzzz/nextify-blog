package com.nextify.blog.common.aop;

import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.annotaion.RequirePermission;
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
 * 权限校验 AOP 切面
 * 拦截 @RequirePermission 注解的方法，进行权限校验
 */
@Slf4j
@Aspect
@Component
public class RequirePermissionAspect {

    @Autowired
    private PermissionService permissionService;

    @Around("@annotation(com.nextify.blog.common.annotaion.RequirePermission)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequirePermission annotation = method.getAnnotation(RequirePermission.class);

        String[] requiredPerms = annotation.value();
        if (requiredPerms == null || requiredPerms.length == 0) {
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
            log.debug("超级管理员 {}, 跳过权限校验", userId);
            return joinPoint.proceed();
        }

        Set<String> userPerms = permInfo.getPermissionCodes();
        RequirePermission.Logic logic = annotation.logic();

        boolean hasPermission;
        if (logic == RequirePermission.Logic.OR) {
            // OR 逻辑：任一权限即可
            hasPermission = false;
            for (String perm : requiredPerms) {
                if (userPerms != null && userPerms.contains(perm)) {
                    hasPermission = true;
                    break;
                }
            }
        } else {
            // AND 逻辑：必须拥有所有权限
            hasPermission = true;
            for (String perm : requiredPerms) {
                if (userPerms == null || !userPerms.contains(perm)) {
                    hasPermission = false;
                    break;
                }
            }
        }

        if (!hasPermission) {
            log.warn("用户 {} 权限校验失败, 需要: {}, 逻辑: {}",
                userId, String.join(",", requiredPerms), logic);
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        return joinPoint.proceed();
    }
}
