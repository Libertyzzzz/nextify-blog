package com.nextify.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.AddUserDto;
import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.dto.UpdateUserDto;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.service.SysUserService;
import com.nextify.blog.vo.SysUserVO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    @GetMapping("/info")
    public Result<SysUser> getUserInfo() {
        SysUser user = sysUserService.findByUserId(UserContextHolder.getUserId());
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    @RequirePermission("system:user")
    @GetMapping("/page")
    public Result<Page<SysUserVO>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        Page<SysUserVO> page = sysUserService.listUsersPage(current, size, keyword, status);
        return Result.success(page);
    }

    @RequirePermission("system:user:add")
    @PostMapping
    public Result<SysUser> create(@Valid @RequestBody AddUserDto dto) {
        SysUser user = sysUserService.createUser(dto);
        return Result.success(user);
    }

    @RequirePermission("system:user:edit")
    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateUserDto dto) {
        SysUser user = sysUserService.updateUser(id, dto);
        return Result.success(user);
    }

    @Deprecated
    @RequirePermission("system:user:edit")
    @PostMapping("/save")
    public Result<Boolean> saveUser(@RequestBody SysUserSaveDto request) {
        return Result.success(sysUserService.saveUser(request));
    }
}