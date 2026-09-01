package com.nextify.blog.common.util;

import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 权限检查工具类
 * 提供编程式权限校验方法，方便在业务代码中动态判断
 *
 * 使用示例:
 *   PermissionChecker.hasPermission("article:create");
 *   PermissionChecker.hasAnyPermission("article:create", "article:edit");
 *   PermissionChecker.hasRole("ROLE_ADMIN");
 */
@Component
public class PermissionChecker {

    private static PermissionService permissionService;

    @Autowired
    public void setPermissionService(PermissionService permissionService) {
        PermissionChecker.permissionService = permissionService;
    }

    /**
     * 判断当前用户是否拥有指定权限 (AND 逻辑)
     */
    public static boolean hasPermission(String... permCodes) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || permissionService == null) {
            return false;
        }
        return permissionService.hasPermission(userId, permCodes);
    }

    /**
     * 判断当前用户是否拥有任一指定权限 (OR 逻辑)
     */
    public static boolean hasAnyPermission(String... permCodes) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || permissionService == null) {
            return false;
        }
        return permissionService.hasAnyPermission(userId, permCodes);
    }

    /**
     * 判断当前用户是否拥有指定角色
     */
    public static boolean hasRole(String roleCode) {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || permissionService == null) {
            return false;
        }
        return permissionService.hasRole(userId, roleCode);
    }

    /**
     * 判断当前用户是否超级管理员
     */
    public static boolean isSuperAdmin() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || permissionService == null) {
            return false;
        }
        return permissionService.isSuperAdmin(userId);
    }

    /**
     * 获取当前用户权限信息
     */
    public static UserPermissionInfo getCurrentUserPermissionInfo() {
        Long userId = UserContextHolder.getUserId();
        if (userId == null || permissionService == null) {
            return null;
        }
        return permissionService.getUserPermissionInfo(userId);
    }
}
