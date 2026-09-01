package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体类
 * 对应数据库表 sys_user_role
 */
@Data
@TableName("sys_user_role")
public class SysUserRole implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID → sys_user.id */
    private Long userId;

    /** 角色ID → sys_role.id */
    private Long roleId;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
