package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.PermissionTreeDto;
import com.nextify.blog.entity.SysPermission;
import com.nextify.blog.vo.PermissionTreeVO;

import java.util.List;

/**
 * 权限管理服务接口
 * 用于权限(菜单/按钮/API)的CRUD管理
 */
public interface PermissionManageService {

    /**
     * 分页查询权限列表
     */
    Page<SysPermission> pagePermissions(int current, int size, String keyword, Integer permType);

    /**
     * 获取所有权限 (树形结构)
     */
    List<SysPermission> listAll();

    /**
     * 获取权限树
     */
    List<PermissionTreeVO> getPermissionTree(PermissionTreeDto request);

    /**
     * 根据ID获取权限
     */
    SysPermission getById(Long id);

    /**
     * 新增权限
     */
    SysPermission createPermission(SysPermission permission);

    /**
     * 更新权限
     */
    SysPermission updatePermission(SysPermission permission);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 根据类型获取权限列表
     */
    List<SysPermission> listByType(Integer permType);
}
