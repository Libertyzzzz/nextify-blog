package com.nextify.blog.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 用户权限信息 DTO
 * 包含用户角色集合和权限编码集合
 */
@Data
public class UserPermissionInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 角色编码集合 */
    private Set<String> roleCodes;

    /** 权限编码集合 */
    private Set<String> permissionCodes;

    /** 数据权限范围 */
    private Integer dataScope;

    /** 是否超级管理员 */
    private Boolean isSuperAdmin;

    /** 过期时间戳 (毫秒) */
    private Long expireAt;
}
