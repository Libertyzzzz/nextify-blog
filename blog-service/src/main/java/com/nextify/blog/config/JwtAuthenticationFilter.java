package com.nextify.blog.config;

import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.UserPermissionInfo;
import com.nextify.blog.entity.SysRole;
import com.nextify.blog.mapper.SysRoleMapper;
import com.nextify.blog.service.PermissionService;
import com.nextify.blog.utils.JwtUtils;
import io.jsonwebtoken.Claims;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private PermissionService permissionService;
    @Autowired
    private SysRoleMapper roleMapper;

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