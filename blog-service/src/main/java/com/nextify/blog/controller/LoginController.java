package com.nextify.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 管理员登录
     * POST /api/auth/login
     */
    @PublicApi
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

        // 3. 初次登陆,生成 Token
        String token = jwtUtils.createToken(user.getUsername(), null);

        // 4. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);
        data.put("expire", jwtUtils.getClaimsByToken(token).getExpiration().getTime());
        // 抹除密码敏感信息
        user.setPassword("******");

        return Result.success(data);
    }

    /**
     * Tonen刷新 每次续约30min
     */
    @PublicApi
    @PostMapping("/refresh")
    public Result<?> refreshToken(@RequestHeader("Authorization") String token) {
        Claims claims = jwtUtils.getClaimsByToken(token);

        if (claims == null) {
            return Result.fail(ResultCode.TOKEN_INVALID);
        }
        Long initLoginTime = claims.get("initLoginTime", Long.class);

        // 检查是否超过最大刷新窗口
        if (initLoginTime == null || jwtUtils.isMaxExpired(claims)) {
            return Result.fail(ResultCode.MAX_EXPIRED);
        }

        // 生成新 Token
        String newToken = jwtUtils.createToken(claims.getSubject(), initLoginTime);

        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("expire", jwtUtils.getClaimsByToken(newToken).getExpiration().getTime());
        return Result.success(data);
    }
}