package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统管理员实体类
 * 对应数据库表 sys_user
 */
@Data
@TableName("sys_user")
public class SysUser implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号 */
    private String username;

    /** 加密密码 (存储时需使用 BCrypt 强度加密) */
    private String password;

    /** 博主昵称 */
    private String nickname;

    /** 头像链接 */
    private String avatar;

    /** 联系邮箱 */
    private String email;

    /** 个人格言 */
    private String motto;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 最后登录时间 */
    private LocalDateTime lastLoginTime;
}