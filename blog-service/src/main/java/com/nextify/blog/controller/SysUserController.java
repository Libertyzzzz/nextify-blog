package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口 - 内部测试
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysUserService sysUserService;
    /**
     * 根据ID查询用户信息 (仅用于验证环境是否跑通)
     */
    @GetMapping("/{id}")
    public Result<SysUser> getUserInfo(@PathVariable Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        // 注意：生产环境应屏蔽密码，此处仅为演示
        user.setPassword("******");
        return Result.success(user);
    }
    @PostMapping("/save")
    public Result<Boolean> saveUser(@RequestBody SysUserSaveDto request) {

        return Result.success(sysUserService.saveUser(request));
    }
}