package com.nextify.blog.service;

import com.nextify.blog.dto.UserPermissionInfo;

import java.util.List;
import java.util.Set;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 获取用户权限信息 (从缓存加载，缓存未命中则从DB回源)
     */
    UserPermissionInfo getUserPermissionInfo(Long userId);

    /**
     * 从数据库加载用户权限信息
     */
    UserPermissionInfo loadUserPermissionFromDb(Long userId);

    /**
     * 缓存用户权限信息
     */
    void cacheUserPermission(Long userId, UserPermissionInfo info);

    /**
     * 失效用户权限缓存
     */
    void invalidateUserPermission(Long userId);

    /**
     * 失效角色下所有用户的权限缓存
     */
    void invalidateRolePermissions(Long roleId);

    /**
     * 判断用户是否拥有指定权限 (AND 逻辑)
     */
    boolean hasPermission(Long userId, String... permCodes);

    /**
     * 判断用户是否拥有任一指定权限 (OR 逻辑)
     */
    boolean hasAnyPermission(Long userId, String... permCodes);

    /**
     * 判断用户是否拥有指定角色
     */
    boolean hasRole(Long userId, String roleCode);

    /**
     * 判断是否超级管理员
     */
    boolean isSuperAdmin(Long userId);

    /**
     * 根据权限编码集合获取权限信息
     */
    UserPermissionInfo getPermissionInfo(Long userId);
}
