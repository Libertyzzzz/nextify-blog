package com.nextify.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.dto.PermissionTreeDto;
import com.nextify.blog.entity.SysPermission;
import com.nextify.blog.service.PermissionManageService;
import com.nextify.blog.vo.PermissionTreeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/permission")
public class PermissionController {

    @Autowired
    private PermissionManageService permissionManageService;

    /**
     * 分页查询权限列表
     */
    @RequirePermission("system:permission")
    @GetMapping("/page")
    public Result<Page<SysPermission>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer permType) {
        Page<SysPermission> page = permissionManageService.pagePermissions(current, size, keyword, permType);
        return Result.success(page);
    }

    /**
     * 获取所有权限列表 (扁平)
     */
    @RequirePermission("system:permission")
    @GetMapping("/list")
    public Result<List<SysPermission>> listAll() {
        List<SysPermission> permissions = permissionManageService.listAll();
        return Result.success(permissions);
    }

    /**
     * 获取权限树
     */
    @RequirePermission("system:permission")
    @GetMapping("/tree")
    public Result<List<PermissionTreeVO>> tree(PermissionTreeDto request) {
        List<PermissionTreeVO> tree = permissionManageService.getPermissionTree(request);
        return Result.success(tree);
    }

    /**
     * 根据类型获取权限列表
     */
    @RequirePermission("system:permission")
    @GetMapping("/type/{permType}")
    public Result<List<SysPermission>> listByType(@PathVariable Integer permType) {
        List<SysPermission> permissions = permissionManageService.listByType(permType);
        return Result.success(permissions);
    }

    /**
     * 根据ID获取权限详情
     */
    @RequirePermission("system:permission")
    @GetMapping("/{id}")
    public Result<SysPermission> getById(@PathVariable Long id) {
        SysPermission permission = permissionManageService.getById(id);
        if (permission == null) {
            return Result.fail("权限不存在");
        }
        return Result.success(permission);
    }

    /**
     * 新增权限
     */
    @RequirePermission("system:permission:add")
    @PostMapping
    public Result<SysPermission> create(@RequestBody SysPermission permission) {
        SysPermission created = permissionManageService.createPermission(permission);
        return Result.success(created);
    }

    /**
     * 更新权限
     */
    @RequirePermission("system:permission:edit")
    @PutMapping
    public Result<SysPermission> update(@RequestBody SysPermission permission) {
        SysPermission updated = permissionManageService.updatePermission(permission);
        return Result.success(updated);
    }

    /**
     * 删除权限
     */
    @RequirePermission("system:permission:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        permissionManageService.deletePermission(id);
        return Result.success();
    }
}