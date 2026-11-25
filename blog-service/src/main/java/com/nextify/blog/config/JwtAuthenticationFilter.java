package com.nextify.blog.config;

import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.controller.SitemapController;
import com.nextify.blog.dto.PermissionInfo;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.SysRole;
import com.nextify.blog.mapper.SysRoleMapper;
import com.nextify.blog.service.PermissionService;
import com.nextify.blog.service.RoleService;
import com.nextify.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    private static final Long ROLE_INTERVIEWER_ID = 5L;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private SysRoleMapper roleMapper;
    @Resource
    private RoleService roleService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            String token = request.getHeader("Authorization");

            if (!StringUtils.hasText(token)) {
                chain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims == null || jwtUtils.isTokenExpired(claims)) {
                chain.doFilter(request, response);
                return;
            }

            String username = claims.getSubject();
            Long userId = claims.get("userId", Long.class);
            UserContextHolder.setUserId(userId);

            // 只读预览链接账号 (-999L)：放行所有 GET 查询，拦截后台写操作
            if (userId != null && userId == -999L) {
                String method = request.getMethod();
                String uri = request.getRequestURI();
                boolean isReadSafe = "GET".equalsIgnoreCase(method)
                        || "OPTIONS".equalsIgnoreCase(method)
                        || (uri != null && (uri.endsWith("/auth/logout") || uri.contains("/v2/assessment")));
                if (!isReadSafe) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":403,\"message\":\"当前为只读预览权限，不支持修改或删除数据\",\"data\":null}");
                    return;
                }

                UserPermissionInfo demoPermInfo = new UserPermissionInfo();
                demoPermInfo.setUserId(userId);
                demoPermInfo.setIsSuperAdmin(false);
                Set<String> demoRoles = Set.of("ROLE_INTERVIEWER", "ROLE_ADMIN");
                List<PermissionInfo> permissions =  roleService.getRolePermissionsByRole(ROLE_INTERVIEWER_ID);


//                Set<String> demoPerms = Set.of(
//                        "dashboard",
//                        "dashboard:article",
//                        "dashboard:category",
//                        "dashboard:tag",
//                        "dashboard:comment",
//                        "dashboard:media",
//                        "dashboard:sensitive",
//                        "dashboard:knowledge",
//                        "dashboard:knowledge:index",
//                        "system",
//                        "system:user",
//                        "system:role",
//                        "system:permission"
//                );
                Set<String> demoPerms = Optional.ofNullable(permissions)
                    .orElseGet(Collections::emptyList)
                    .stream()
                    .map(PermissionInfo::getPermCode)
                    .filter(Objects::nonNull)
                    .filter(code -> !code.isBlank())
                        .collect(Collectors.toSet());
                demoPermInfo.setRoleCodes(demoRoles);
                demoPermInfo.setPermissionCodes(demoPerms);
                UserContextHolder.setPermissionInfo(demoPermInfo);

                List<GrantedAuthority> demoAuthorities = new ArrayList<>();
                demoRoles.forEach(r -> demoAuthorities.add(new SimpleGrantedAuthority(r)));
                demoPerms.forEach(p -> demoAuthorities.add(new SimpleGrantedAuthority(p)));

                UsernamePasswordAuthenticationToken demoAuth =
                        new UsernamePasswordAuthenticationToken(username, null, demoAuthorities);
                demoAuth.setDetails(demoPermInfo);
                SecurityContextHolder.getContext().setAuthentication(demoAuth);
                chain.doFilter(request, response);
                return;
            }

            UserPermissionInfo permInfo = null;
            Collection<GrantedAuthority> authorities = new ArrayList<>();

            try {
                if (userId != null) {
                    permInfo = permissionService.getUserPermissionInfo(userId);

                    List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
                    Set<String> roleCodes = permInfo.getRoleCodes();
                    Set<String> permCodes = permInfo.getPermissionCodes();

                    if (roleCodes != null) {
                        for (String roleCode : roleCodes) {
                            String roleWithPrefix = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
                            grantedAuthorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                        }
                    }

                    if (Boolean.TRUE.equals(permInfo.getIsSuperAdmin())) {
                        try {
                            List<SysRole> allRoles = roleMapper.selectList(null);
                            for (SysRole role : allRoles) {
                                String roleCode = role.getRoleCode();
                                String roleWithPrefix = roleCode.startsWith("ROLE_") ? roleCode : "ROLE_" + roleCode;
                                if (grantedAuthorities.stream().noneMatch(
                                        g -> g.getAuthority().equals(roleWithPrefix))) {
                                    grantedAuthorities.add(new SimpleGrantedAuthority(roleWithPrefix));
                                }
                            }
                            log.debug("超级管理员 {}, 已追加全部 {} 个角色", userId, allRoles.size());
                        } catch (Exception ex) {
                            log.warn("超管追加角色失败, userId={}, error={}", userId, ex.getMessage());
                        }
                    }

                    if (permCodes != null) {
                        for (String permCode : permCodes) {
                            grantedAuthorities.add(new SimpleGrantedAuthority(permCode));
                        }
                    }
                    authorities = grantedAuthorities;

                    UserContextHolder.setPermissionInfo(permInfo);
                }
            } catch (Exception e) {
                log.warn("加载用户权限失败, userId={}, error={}", userId, e.getMessage());
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(permInfo);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.remove();
        }
    }
}