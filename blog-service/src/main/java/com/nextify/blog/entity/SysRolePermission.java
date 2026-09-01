package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体类
 * 对应数据库表 sys_role_permission
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID → sys_role.id */
    private Long roleId;

    /** 权限ID → sys_permission.id */
    private Long permissionId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
