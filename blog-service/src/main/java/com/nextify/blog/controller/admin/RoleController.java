package com.nextify.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.SysRole;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.service.RoleService;
import com.nextify.blog.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/admin/role")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private SysUserService userService;

    @Autowired
    private SysUserMapper userMapper;

    /**
     * 分页查询角色列表
     */
    @RequirePermission("system:role")
    @GetMapping("/page")
    public Result<Page<SysRole>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysRole> page = roleService.pageRoles(current, size, keyword);
        return Result.success(page);
    }

    /**
     * 获取所有角色列表 (下拉选择用)
     */
    @RequirePermission("system:role")
    @GetMapping("/list")
    public Result<List<SysRole>> listAll() {
        List<SysRole> roles = roleService.listAll();
        return Result.success(roles);
    }

    /**
     * 根据ID获取角色详情
     */
    @RequirePermission("system:role")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = roleService.getById(id);
        if (role == null) {
            return Result.fail("角色不存在");
        }
        return Result.success(role);
    }

    /**
     * 新增角色
     */
    @RequirePermission("system:role:add")
    @PostMapping
    public Result<SysRole> create(@RequestBody SysRole role) {
        SysRole created = roleService.createRole(role);
        return Result.success(created);
    }

    /**
     * 更新角色
     */
    @RequirePermission("system:role:edit")
    @PutMapping
    public Result<SysRole> update(@RequestBody SysRole role) {
        SysRole updated = roleService.updateRole(role);
        return Result.success(updated);
    }

    /**
     * 删除角色
     */
    @RequirePermission("system:role:delete")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    /**
     * 获取用户的角色列表
     */
    @RequirePermission("system:user:edit")
    @GetMapping("/user/{userId}")
    public Result<List<SysRole>> getUserRoles(@PathVariable String userId) {
        List<SysRole> roles = roleService.getUserRoles(userId);
        return Result.success(roles);
    }

    /**
     * 为用户分配角色
     */
    @RequirePermission("system:user:edit")
    @PostMapping("/assign-user")
    public Result<Void> assignRolesToUser(
            @RequestParam Long userId,
            @RequestParam List<Long> roleIds) {
        roleService.assignRolesToUser(userId, roleIds);
        return Result.success();
    }

    /**
     * 获取角色的权限ID列表
     */
    @RequirePermission("system:role:edit")
    @GetMapping("/{id}/permissions")
    public Result<List<Long>> getRolePermissions(@PathVariable Long id) {
        List<Long> permIds = roleService.getRolePermissionIds(id);
        return Result.success(permIds);
    }

    /**
     * 为角色分配权限
     */
    @RequirePermission("system:role:edit")
    @PostMapping("/{id}/permissions")
    public Result<Void> assignPermissionsToRole(
            @PathVariable Long id,
            @RequestParam List<Long> permissionIds) {
        roleService.assignPermissionsToRole(id, permissionIds);
        return Result.success();
    }

    /**
     * 获取当前登录用户的权限信息 (前端初始化时调用，允许登录用户访问)
     */
    @GetMapping("/user/permissions")
    public Result<UserPermissionInfo> getUserPermissions() {
        UserPermissionInfo info = roleService.getUserPermissionInfo(UserContextHolder.getUserId());
        return Result.success(info);
    }

    /**
     * 获取用户列表 (用于角色分配时选择用户)
     */
    @RequirePermission("system:user")
    @GetMapping("/users")
    public Result<List<Map<String, Object>>> listUsers() {
        List<SysUser> users = userMapper.selectList(null);
        List<Map<String, Object>> result = users.stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("nickname", u.getNickname());
            map.put("avatar", u.getAvatar());
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }
}