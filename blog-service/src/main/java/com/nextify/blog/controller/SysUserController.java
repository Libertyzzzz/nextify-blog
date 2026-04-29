package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理接口 - 内部测试
 */
@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserMapper sysUserMapper;

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
}