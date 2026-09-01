package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.SysRole;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 分页查询角色列表
     */
    Page<SysRole> pageRoles(int current, int size, String keyword);

    /**
     * 获取所有角色
     */
    List<SysRole> listAll();

    /**
     * 根据ID获取角色
     */
    SysRole getById(Long id);

    /**
     * 根据角色编码获取角色
     */
    SysRole getByCode(String roleCode);

    /**
     * 新增角色
     */
    SysRole createRole(SysRole role);

    /**
     * 更新角色
     */
    SysRole updateRole(SysRole role);

    /**
     * 删除角色 (系统内置角色不可删除)
     */
    void deleteRole(Long id);

    /**
     * 为用户分配角色
     */
    void assignRolesToUser(Long userId, List<Long> roleIds);

    /**
     * 移除用户的角色
     */
    void removeRolesFromUser(Long userId, List<Long> roleIds);

    /**
     * 获取用户的角色列表
     */
    List<SysRole> getUserRoles(String userId);

    /**
     * 为角色分配权限
     */
    void assignPermissionsToRole(Long roleId, List<Long> permissionIds);

    /**
     * 移除角色的权限
     */
    void removePermissionsFromRole(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色的权限ID列表
     */
    List<Long> getRolePermissionIds(Long roleId);

    /**
     * 获取用户权限信息 (包含角色和权限)
     */
    UserPermissionInfo getUserPermissionInfo(Long userId);
}
