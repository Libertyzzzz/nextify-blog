package com.nextify.blog.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.ResultCode;
import com.nextify.blog.common.annotaion.PublicApi;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.common.annotaion.RequiredLogin;
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.common.properties.JwtAuthenticationProperty;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.service.SysUserService;
import com.nextify.blog.utils.JwtUtils;
import com.nextify.blog.utils.RedisUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private JwtAuthenticationProperty jwtAuthenticationProperty;

    @Resource
    private SysUserService userService;

    private static final  String REFRESH_TOKEN_KEY = "REFRESH_TOKEN:";
    private static final String PREVIEW_CODE_REDIS_KEY = "PREVIEW_LINK:CODE";
    private static final String PREVIEW_ENABLED_REDIS_KEY = "PREVIEW_LINK:ENABLED";

    @Value("${nextify.preview.default-code:nx_preview_2026}")
    private String defaultPreviewCode;

    /**
     * 管理员登录
     * POST /api/auth/login
     */
    @PublicApi
    @PostMapping("/login")
    public Result<?> login(@RequestBody Map<String, String> loginParams,
                           HttpServletResponse response) {
        log.info("login controller entered....");
        String username = loginParams.get("username");
        String password = loginParams.get("password");

        // 1. 查询用户
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getUsername, username)
                    //.eq(SysUser::getStatus, 1)
        );
        if(user == null)
            return Result.fail(ResultCode.USER_NOT_EXIST);
        if(user.getStatus() == 0)
            return Result.fail(ResultCode.USER_FORBIDDEN);
        // 2. 校验用户是否存在及密码是否匹配
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.fail(ResultCode.PASSWORD_ERROR);
        }

        // 3. 初次登陆,生成 Token
        String accessToken = jwtUtils.createToken(user.getUsername(), user.getUserId());
        // 生成refreshToken
        String refreshToken = UUID.randomUUID().toString().replace("-", "");
        long initLoginTime = System.currentTimeMillis();
        long maxExpireAt = initLoginTime + jwtAuthenticationProperty.getMaxRefresh() * 1000;
        redisUtils.set(
            REFRESH_TOKEN_KEY + refreshToken,
            user.getUserId().toString() + "|" + initLoginTime + "|" + maxExpireAt,
            jwtAuthenticationProperty.getRefresh(),
            TimeUnit.SECONDS
        );
        response.addCookie(createRefreshCookie(refreshToken));
        // 4. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);
        data.put("user", user);
        data.put("expire", jwtUtils.getClaimsByToken(accessToken).getExpiration().getTime());
        // 抹除密码敏感信息
        user.setPassword("******");

        return Result.success(data);
    }

    /**
     * 专属预览链接静默登录（校验 preview 凭证，签发只读观察员令牌）
     * POST /api/auth/preview-login
     */
    @PublicApi
    @PostMapping("/preview-login")
    public Result<?> previewLogin(@RequestBody Map<String, String> params) {
        String code = params != null ? params.get("code") : null;
        if (!StringUtils.hasText(code)) {
            return Result.fail(ResultCode.FORBIDDEN.getCode(), "预览凭证不能为空");
        }

        String enabledFlag = null;
        String activeCode = null;
        try {
            enabledFlag = redisUtils.get(PREVIEW_ENABLED_REDIS_KEY);
            activeCode = redisUtils.get(PREVIEW_CODE_REDIS_KEY);
        } catch (Exception e) {
            log.warn("读取 Redis 预览链接配置失败，降级使用默认配置: {}", e.getMessage());
        }

        boolean isEnabled = enabledFlag == null || !"false".equalsIgnoreCase(enabledFlag);
        if (!isEnabled) {
            return Result.fail(ResultCode.FORBIDDEN.getCode(), "该预览链接已关闭访问");
        }

        String expectedCode = StringUtils.hasText(activeCode) ? activeCode : defaultPreviewCode;
        if (!expectedCode.equals(code.trim())) {
            return Result.fail(ResultCode.FORBIDDEN.getCode(), "预览链接无效或已过期");
        }

        Long viewerUserId = -999L;
        String viewerUsername = "viewer";
        String accessToken = jwtUtils.createToken(viewerUsername, viewerUserId);

        SysUser viewerUser = new SysUser();
        viewerUser.setId(viewerUserId);
        viewerUser.setUsername(viewerUsername);
        viewerUser.setNickname("只读预览账号");
        viewerUser.setEmail("viewer@nextify.cn");
        viewerUser.setMotto("只读预览权限 · 全模块可见");
        viewerUser.setStatus(1);
        viewerUser.setPassword("******");

        Map<String, Object> data = new HashMap<>();
        data.put("token", accessToken);
        data.put("user", viewerUser);
        data.put("expire", jwtUtils.getClaimsByToken(accessToken).getExpiration().getTime());
        return Result.success(data);
    }

    /**
     * 获取当前预览链接配置（超管/权限管理专用）
     * GET /api/auth/preview-link
     */
    @RequirePermission("system:permission")
    @GetMapping("/preview-link")
    public Result<Map<String, Object>> getPreviewLinkConfig() {
        String enabledFlag = redisUtils.get(PREVIEW_ENABLED_REDIS_KEY);
        String activeCode = redisUtils.get(PREVIEW_CODE_REDIS_KEY);
        boolean isEnabled = enabledFlag == null || !"false".equalsIgnoreCase(enabledFlag);
        String code = StringUtils.hasText(activeCode) ? activeCode : defaultPreviewCode;

        Map<String, Object> data = new HashMap<>();
        data.put("enabled", isEnabled);
        data.put("code", code);
        return Result.success(data);
    }

    /**
     * 重置或开关预览链接（超管/权限管理专用）
     * POST /api/auth/preview-link/reset
     */
    @RequirePermission("system:permission")
    @PostMapping("/preview-link/reset")
    public Result<Map<String, Object>> updatePreviewLinkConfig(@RequestBody(required = false) Map<String, Object> body) {
        String action = body != null && body.get("action") != null ? String.valueOf(body.get("action")) : "reset";
        if ("toggle".equalsIgnoreCase(action) && body != null && body.get("enabled") != null) {
            boolean enabled = Boolean.parseBoolean(String.valueOf(body.get("enabled")));
            redisUtils.set(PREVIEW_ENABLED_REDIS_KEY, String.valueOf(enabled));
        } else {
            String newCode = "nx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            redisUtils.set(PREVIEW_CODE_REDIS_KEY, newCode);
            redisUtils.set(PREVIEW_ENABLED_REDIS_KEY, "true");
        }
        return getPreviewLinkConfig();
    }

    /**
     * 创建 HttpOnly Cookie
     */
    private Cookie createRefreshCookie(String refreshToken) {
        Cookie cookie = new Cookie(jwtAuthenticationProperty.getCookieName(), refreshToken);
        cookie.setHttpOnly(jwtAuthenticationProperty.getCookieHttpOnly());
        cookie.setSecure(jwtAuthenticationProperty.getCookieSecure());
        cookie.setPath(jwtAuthenticationProperty.getCookiePath());
        cookie.setMaxAge(jwtAuthenticationProperty.getRefresh().intValue());
        if (jwtAuthenticationProperty.getCookieDomain() != null
            && !jwtAuthenticationProperty.getCookieDomain().isEmpty()) {
            cookie.setDomain(jwtAuthenticationProperty.getCookieDomain());
        }
        return cookie;
    }

    /**
     * 从请求中获取 Refresh Token Cookie
     */
    private String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (jwtAuthenticationProperty.getCookieName().equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    /**
     * Token刷新 每次续约30min
     * Token刷新：每次从HttpOnly Cookie读取Refresh Token
     */
    @PublicApi
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(
                        HttpServletRequest request,
                        HttpServletResponse response) {
        String refreshToken = getRefreshTokenFromCookie(request);
        if(refreshToken == null){
            return Result.fail(ResultCode.TOKEN_INVALID);
        }
        String payload = redisUtils.get(REFRESH_TOKEN_KEY + refreshToken);
        if (payload == null)
            return Result.fail(ResultCode.MAX_EXPIRED);
        String[] parts = payload.split("\\|");
        String userId = parts[0];
        long initLoginTime = Long.parseLong(parts[1]);
        long maxExpireAt = Long.parseLong(parts[2]);

        if (System.currentTimeMillis() > maxExpireAt) {
            return Result.fail(ResultCode.MAX_EXPIRED);
        }
        if(userId == null || userId.isEmpty()){
            return Result.fail(ResultCode.USER_NOT_EXIST);
        }
        // Claims claims = jwtUtils.getClaimsByToken(token);

        // Judge by refresh token
//        if (claims == null) {
//            return Result.fail(ResultCode.TOKEN_INVALID);
//        }
        // Long initLoginTime = claims.get("initLoginTime", Long.class);

        // create new token
        SysUser user = userService.findByUserId(Long.valueOf(userId));
        if(ObjectUtils.isEmpty(user))
            return Result.fail(ResultCode.USER_NOT_EXIST);
        String newToken = jwtUtils.createToken(user.getUsername(), user.getUserId());
        // refresh token rotation
        redisUtils.deleteKey(REFRESH_TOKEN_KEY + refreshToken);
        String newRefreshToken = UUID.randomUUID().toString().replace("-", "");
        redisUtils.set(
            REFRESH_TOKEN_KEY + newRefreshToken,
            userId + "|" + initLoginTime +  "|" + maxExpireAt,
            jwtAuthenticationProperty.getRefresh(),
            TimeUnit.SECONDS
        );
        // write cookie
        response.addCookie(createRefreshCookie(newRefreshToken));
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        data.put("expire", jwtUtils.getClaimsByToken(newToken).getExpiration().getTime());
        return Result.success(data);
    }


    @RequiredLogin
    @PostMapping("/logout")
    public Result<Boolean> logout(HttpServletRequest request, HttpServletResponse response){
        String refreshToken = getRefreshTokenFromCookie(request);
        if(refreshToken != null){
            redisUtils.deleteKey(REFRESH_TOKEN_KEY + refreshToken);
        }
        response.addCookie(clearRefreshCookie());
        return Result.success(Boolean.TRUE);
    }


    /**
     * 清除 Cookie（max-age=0）
     */
    private Cookie clearRefreshCookie() {
        Cookie cookie = new Cookie(jwtAuthenticationProperty.getCookieName(), "");
        cookie.setHttpOnly(jwtAuthenticationProperty.getCookieHttpOnly());
        cookie.setSecure(jwtAuthenticationProperty.getCookieSecure());
        cookie.setPath(jwtAuthenticationProperty.getCookiePath());
        cookie.setMaxAge(0);
        if (jwtAuthenticationProperty.getCookieDomain() != null
            && !jwtAuthenticationProperty.getCookieDomain().isEmpty()) {
            cookie.setDomain(jwtAuthenticationProperty.getCookieDomain());
        }
        return cookie;
    }
}