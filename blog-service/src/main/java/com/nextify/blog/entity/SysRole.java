package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色实体类
 * 对应数据库表 sys_role
 */
@Data
@TableName("sys_role")
public class SysRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色编码 (唯一标识，如 ROLE_ADMIN) */
    private String roleCode;

    /** 角色名称 (显示用) */
    private String roleName;

    /** 角色描述 */
    private String description;

    /** 排序 (数值越小越靠前) */
    private Integer sort;

    /** 数据权限范围 1=全部 2=本部门 3=本部门及以下 4=仅本人 */
    private Integer dataScope;

    /** 状态 1=启用 0=禁用 */
    private Integer status;

    /** 系统内置角色不可删除 */
    private Integer isSystem;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
