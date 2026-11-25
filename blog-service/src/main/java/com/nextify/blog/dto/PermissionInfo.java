package com.nextify.blog.dto;

import lombok.Data;

@Data
public class PermissionInfo {
    /** 父级ID (0=顶级) */
    private Long parentId;

    /** 权限编码 (全局唯一，如 article:create) */
    private String permCode;

    /** 权限名称 (显示用) */
    private String permName;

    /** 权限类型 1=菜单 2=按钮 3=API接口 */
    private Integer permType;

    /** 路由路径 (菜单用) */
    private String path;

    /** 前端组件路径 (菜单用) */
    private String component;
}
