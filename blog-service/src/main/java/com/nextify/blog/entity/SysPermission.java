package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 权限实体类 (菜单 + 按钮 + API)
 * 对应数据库表 sys_permission
 */
@Data
@TableName("sys_permission")
public class SysPermission implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 菜单图标 (菜单用) */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 是否可见 1=显示 0=隐藏 */
    private Integer visible;

    /** 状态 1=启用 0=禁用 */
    private Integer status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
