package com.nextify.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ResultCode;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录鉴权控制器
 * 处理博主登录，签发 JWT 令牌
 */
@RestController
@RequestMapping("/auth")
@Slf4j
public class LoginController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 管理员登录
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginParams) {
        log.info("login controller entered....");
        String username = loginParams.get("username");
        String password = loginParams.get("password");

        // 1. 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)
        );
        if(user == null)
            return Result.fail(ResultCode.USER_NOT_EXIST);
        // 2. 校验用户是否存在及密码是否匹配
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR);
        }

        // 3. 生成 Token
        String token = jwtUtils.createToken(user.getUsername());

        // 4. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        // 抹除密码敏感信息
        user.setPassword("******");

        return Result.success(data);
    }
}