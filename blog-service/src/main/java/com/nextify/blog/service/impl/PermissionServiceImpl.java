package com.nextify.blog.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.*;
import com.nextify.blog.mapper.*;
import com.nextify.blog.service.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionServiceImpl implements PermissionService {

    private static final long CACHE_TTL_MINUTES = 30;
    private static final String PERMS_CACHE_PREFIX = "perms:user:";
    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    @Autowired
    private SysUserRoleMapper userRoleMapper;
    @Autowired
    private SysRoleMapper roleMapper;
    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;
    @Autowired
    private SysPermissionMapper permissionMapper;
    @Autowired
    private SysUserMapper userMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public UserPermissionInfo getUserPermissionInfo(Long userId) {
        if (userId == null) {
            return createEmptyInfo();
        }

        try {
            UserPermissionInfo cached = getFromCache(userId);
            if (cached != null
                && cached.getExpireAt() != null
                && cached.getExpireAt() > System.currentTimeMillis()) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("从Redis加载权限缓存失败, userId={}, error={}", userId, e.getMessage());
        }

        UserPermissionInfo info = loadUserPermissionFromDb(userId);

        try {
            cacheUserPermission(userId, info);
        } catch (Exception e) {
            log.warn("写入Redis权限缓存失败, userId={}, error={}", userId, e.getMessage());
        }

        return info;
    }

    @Override
    public UserPermissionInfo loadUserPermissionFromDb(Long userId) {
        UserPermissionInfo info = createEmptyInfo();
        info.setUserId(userId);

        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (userRoles.isEmpty()) {
            return info;
        }

        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        List<SysRole> roles = roleMapper.selectBatchIds(roleIds);
        Set<String> roleCodes = roles.stream()
            .filter(r -> r.getStatus() == 1)
            .map(SysRole::getRoleCode)
            .collect(Collectors.toSet());
        info.setRoleCodes(roleCodes);

        boolean isSuperAdmin = roleCodes.contains(ROLE_SUPER_ADMIN);
        info.setIsSuperAdmin(isSuperAdmin);

        if (isSuperAdmin) {
            List<SysPermission> allPermissions = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getStatus, 1));
            info.setPermissionCodes(allPermissions.stream()
                .map(SysPermission::getPermCode).collect(Collectors.toSet()));
            info.setDataScope(1);
            return info;
        }

        List<SysRolePermission> rolePermissions = rolePermissionMapper.selectList(
            new LambdaQueryWrapper<SysRolePermission>().in(SysRolePermission::getRoleId, roleIds));

        if (!rolePermissions.isEmpty()) {
            List<Long> permIds = rolePermissions.stream()
                .map(SysRolePermission::getPermissionId).collect(Collectors.toList());
            List<SysPermission> permissions = permissionMapper.selectBatchIds(permIds);
            info.setPermissionCodes(permissions.stream()
                .filter(p -> p.getStatus() == 1)
                .map(SysPermission::getPermCode).collect(Collectors.toSet()));
        }

        Optional<Integer> maxDataScope = roles.stream()
            .map(SysRole::getDataScope).filter(Objects::nonNull).max(Integer::compareTo);
        info.setDataScope(maxDataScope.orElse(1));

        return info;
    }

    @Override
    public void cacheUserPermission(Long userId, UserPermissionInfo info) {
        if (userId == null || info == null) {
            return;
        }
        try {
            info.setExpireAt(System.currentTimeMillis() + CACHE_TTL_MINUTES * 60 * 1000);
            redisTemplate.opsForValue().set(PERMS_CACHE_PREFIX + userId,
                JSON.toJSONString(info), CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.warn("缓存用户权限失败, userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void invalidateUserPermission(Long userId) {
        if (userId == null) {
            return;
        }
        try {
            redisTemplate.delete(PERMS_CACHE_PREFIX + userId);
            log.info("已清除用户权限缓存, userId={}", userId);
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败, userId={}, error={}", userId, e.getMessage());
        }
    }

    @Override
    public void invalidateRolePermissions(Long roleId) {
        if (roleId == null) {
            return;
        }
        try {
            List<SysUserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, roleId));
            for (SysUserRole ur : userRoles) {
                invalidateUserPermission(ur.getUserId());
            }
            log.info("已清除角色下所有用户权限缓存, roleId={}, userCount={}", roleId, userRoles.size());
        } catch (Exception e) {
            log.warn("清除角色权限缓存失败, roleId={}, error={}", roleId, e.getMessage());
        }
    }

    @Override
    public boolean hasPermission(Long userId, String... permCodes) {
        if (userId == null || permCodes == null || permCodes.length == 0) {
            return false;
        }
        UserPermissionInfo info = getUserPermissionInfo(userId);
        if (Boolean.TRUE.equals(info.getIsSuperAdmin())) {
            return true;
        }
        Set<String> userPerms = info.getPermissionCodes();
        if (userPerms == null || userPerms.isEmpty()) {
            return false;
        }
        for (String permCode : permCodes) {
            if (!userPerms.contains(permCode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permCodes) {
        if (userId == null || permCodes == null || permCodes.length == 0) {
            return false;
        }
        UserPermissionInfo info = getUserPermissionInfo(userId);
        if (Boolean.TRUE.equals(info.getIsSuperAdmin())) {
            return true;
        }
        Set<String> userPerms = info.getPermissionCodes();
        if (userPerms == null || userPerms.isEmpty()) {
            return false;
        }
        for (String permCode : permCodes) {
            if (userPerms.contains(permCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasRole(Long userId, String roleCode) {
        if (userId == null || !StringUtils.hasText(roleCode)) {
            return false;
        }
        UserPermissionInfo info = getUserPermissionInfo(userId);
        if (Boolean.TRUE.equals(info.getIsSuperAdmin())) {
            return true;
        }
        Set<String> roles = info.getRoleCodes();
        return roles != null && roles.contains(roleCode);
    }

    @Override
    public boolean isSuperAdmin(Long userId) {
        if (userId == null) {
            return false;
        }
        return Boolean.TRUE.equals(getUserPermissionInfo(userId).getIsSuperAdmin());
    }

    @Override
    public UserPermissionInfo getPermissionInfo(Long userId) {
        return getUserPermissionInfo(userId);
    }

    private UserPermissionInfo getFromCache(Long userId) {
        try {
            String json = redisTemplate.opsForValue().get(PERMS_CACHE_PREFIX + userId);
            if (StringUtils.hasText(json)) {
                return JSON.parseObject(json, UserPermissionInfo.class);
            }
        } catch (Exception e) {
            log.warn("从Redis读取权限缓存异常, userId={}, error={}", userId, e.getMessage());
        }
        return null;
    }

    private UserPermissionInfo createEmptyInfo() {
        UserPermissionInfo info = new UserPermissionInfo();
        info.setRoleCodes(Collections.emptySet());
        info.setPermissionCodes(Collections.emptySet());
        info.setIsSuperAdmin(false);
        info.setDataScope(1);
        return info;
    }
}