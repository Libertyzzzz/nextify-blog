package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.*;
import com.nextify.blog.mapper.*;
import com.nextify.blog.service.PermissionService;
import com.nextify.blog.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    @Autowired
    private SysPermissionMapper permissionMapper;
    @Autowired
    private PermissionService permissionService;

    @Override
    public Page<SysRole> pageRoles(int current, int size, String keyword) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysRole::getRoleName, keyword)
                .or()
                .like(SysRole::getRoleCode, keyword)
            );
        }
        wrapper.orderByAsc(SysRole::getSort);
        return roleMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysRole> listAll() {
        return roleMapper.selectList(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getStatus, 1).orderByAsc(SysRole::getSort));
    }

    @Override
    public SysRole getById(Long id) {
        return roleMapper.selectById(id);
    }

    @Override
    public SysRole getByCode(String roleCode) {
        return roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleCode, roleCode));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole createRole(SysRole role) {
        role.setIsSystem(0);
        role.setStatus(1);
        if (role.getDataScope() == null) role.setDataScope(1);
        if (role.getSort() == null) role.setSort(0);
        roleMapper.insert(role);
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole updateRole(SysRole role) {
        if (roleMapper.selectById(role.getId()) == null) {
            throw new IllegalArgumentException("角色不存在: " + role.getId());
        }
        roleMapper.updateById(role);
        permissionService.invalidateRolePermissions(role.getId());
        return role;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        SysRole existing = roleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("角色不存在: " + id);
        }
        if (existing.getIsSystem() != null && existing.getIsSystem() == 1) {
            throw new IllegalArgumentException("系统内置角色不可删除: " + existing.getRoleName());
        }

        Long userCount = userRoleMapper.selectCount(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount != null && userCount > 0) {
            throw new IllegalArgumentException("该角色下还有 " + userCount + " 个用户，无法删除");
        }

        rolePermissionMapper.delete(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        roleMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRolesToUser(Long userId, List<Long> roleIds) {
        if (userId == null || roleIds == null) return;

        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        for (Long roleId : roleIds) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
        permissionService.invalidateUserPermission(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeRolesFromUser(Long userId, List<Long> roleIds) {
        if (userId == null || roleIds == null) return;
        for (Long roleId : roleIds) {
            userRoleMapper.delete(
                new LambdaQueryWrapper<SysUserRole>()
                    .eq(SysUserRole::getUserId, userId)
                    .eq(SysUserRole::getRoleId, roleId));
        }
        permissionService.invalidateUserPermission(userId);
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        if (userId == null) return Collections.emptyList();

        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) return Collections.emptyList();

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());
        return roleMapper.selectBatchIds(roleIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissionsToRole(Long roleId, List<Long> permissionIds) {
        if (roleId == null || permissionIds == null) return;

        rolePermissionMapper.delete(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        for (Long permId : permissionIds) {
            SysRolePermission rp = new SysRolePermission();
            rp.setRoleId(roleId);
            rp.setPermissionId(permId);
            rolePermissionMapper.insert(rp);
        }
        permissionService.invalidateRolePermissions(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removePermissionsFromRole(Long roleId, List<Long> permissionIds) {
        if (roleId == null || permissionIds == null) return;
        for (Long permId : permissionIds) {
            rolePermissionMapper.delete(
                new LambdaQueryWrapper<SysRolePermission>()
                    .eq(SysRolePermission::getRoleId, roleId)
                    .eq(SysRolePermission::getPermissionId, permId));
        }
        permissionService.invalidateRolePermissions(roleId);
    }

    @Override
    public List<Long> getRolePermissionIds(Long roleId) {
        if (roleId == null) return Collections.emptyList();

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        return rolePermissions.stream().map(SysRolePermission::getPermissionId).collect(Collectors.toList());
    }

    @Override
    public UserPermissionInfo getUserPermissionInfo(Long userId) {
        return permissionService.getUserPermissionInfo(userId);
    }
}