package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.PermissionTreeDto;
import com.nextify.blog.entity.SysPermission;
import com.nextify.blog.entity.SysRolePermission;
import com.nextify.blog.entity.SysUserRole;
import com.nextify.blog.mapper.SysPermissionMapper;
import com.nextify.blog.mapper.SysRolePermissionMapper;
import com.nextify.blog.mapper.SysUserRoleMapper;
import com.nextify.blog.service.PermissionManageService;
import com.nextify.blog.service.PermissionService;
import com.nextify.blog.utils.PermissionVOConverter;
import com.nextify.blog.vo.PermissionTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 权限管理服务实现类
 */
@Slf4j
@Service
public class PermissionManageServiceImpl implements PermissionManageService {

    @Autowired
    private SysPermissionMapper permissionMapper;

    @Autowired
    private SysRolePermissionMapper rolePermissionMapper;

    @Autowired
    private SysUserRoleMapper userRoleMapper;

    @Autowired
    private PermissionService permissionService;

    @Override
    public Page<SysPermission> pagePermissions(int current, int size, String keyword, Integer permType) {
        Page<SysPermission> page = new Page<>(current, size);
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysPermission::getPermName, keyword)
                .or()
                .like(SysPermission::getPermCode, keyword)
            );
        }
        if (permType != null) {
            wrapper.eq(SysPermission::getPermType, permType);
        }
        wrapper.orderByAsc(SysPermission::getSort);
        return permissionMapper.selectPage(page, wrapper);
    }

    @Override
    public List<SysPermission> listAll() {
        return permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .orderByAsc(SysPermission::getSort)
        );
    }

    @Override
    public List<PermissionTreeVO> getPermissionTree(PermissionTreeDto request) {
        LambdaQueryWrapper<SysPermission> wrapper = new LambdaQueryWrapper<>();
        if (request != null && request.getPermType() != null) {
            wrapper.eq(SysPermission::getPermType, request.getPermType());
        }
        if (request == null || Boolean.TRUE.equals(request.getOnlyEnabled())) {
            wrapper.eq(SysPermission::getStatus, 1);
        }
        wrapper.orderByAsc(SysPermission::getSort);
        List<SysPermission> all =  permissionMapper.selectList(wrapper);

        Long startParentId = (request != null && request.getParentId() != null
            ? request.getParentId()
            : 0L);

        return PermissionVOConverter.toTree(all, startParentId);
    }

    private void sortList(List<SysPermission> list) {
        list.sort(Comparator.comparingInt(p -> p.getSort() != null ? p.getSort() : 0));
    }

    @Override
    public SysPermission getById(Long id) {
        return permissionMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermission createPermission(SysPermission permission) {
        permission.setStatus(1);
        permission.setVisible(1);
        if (permission.getSort() == null) {
            permission.setSort(0);
        }
        if (permission.getParentId() == null) {
            permission.setParentId(0L);
        }
        permissionMapper.insert(permission);
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysPermission updatePermission(SysPermission permission) {
        SysPermission existing = permissionMapper.selectById(permission.getId());
        if (existing == null) {
            throw new IllegalArgumentException("权限不存在: " + permission.getId());
        }
        permissionMapper.updateById(permission);

        // 如果权限编码变更，需要清除所有关联用户的缓存
        if (!Objects.equals(existing.getPermCode(), permission.getPermCode())) {
            clearAllUserCaches();
        }
        return permission;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        SysPermission existing = permissionMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("权限不存在: " + id);
        }

        // 检查是否有子权限
        Long childCount = permissionMapper.selectCount(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getParentId, id)
        );
        if (childCount != null && childCount > 0) {
            throw new IllegalArgumentException("该权限下还有子权限，无法删除");
        }

        // 删除角色权限关联
        rolePermissionMapper.delete(
            new LambdaQueryWrapper<SysRolePermission>()
                .eq(SysRolePermission::getPermissionId, id)
        );

        // 删除权限
        permissionMapper.deleteById(id);

        // 清除所有关联用户的缓存
        clearAllUserCaches();
    }

    @Override
    public List<SysPermission> listByType(Integer permType) {
        return permissionMapper.selectList(
            new LambdaQueryWrapper<SysPermission>()
                .eq(SysPermission::getPermType, permType)
                .eq(SysPermission::getStatus, 1)
                .orderByAsc(SysPermission::getSort)
        );
    }

    /**
     * 清除所有用户的权限缓存
     * 权限变更时调用
     */
    private void clearAllUserCaches() {
        try {
            // 查询所有用户
            List<SysUserRole> allUserRoles = userRoleMapper.selectList(null);
            Set<Long> userIds = allUserRoles.stream()
                .map(SysUserRole::getUserId)
                .collect(Collectors.toSet());

            for (Long userId : userIds) {
                permissionService.invalidateUserPermission(userId);
            }
            log.info("已清除 {} 个用户的权限缓存", userIds.size());
        } catch (Exception e) {
            log.warn("清除用户权限缓存失败: {}", e.getMessage());
        }
    }
}
