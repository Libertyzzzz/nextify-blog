package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论记录表
 */
@Data
@TableName("blog_comment")
public class BlogComment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "article_id")
    private Long articleId;

    @TableField(value = "parent_id")
    private Long parentId;

    @TableField(value = "anonymous_id")
    private String anonymousId;

    @TableField(value = "nickname")
    private String nickname;

    @TableField(value = "email")
    private String email;

    @TableField(value = "avatar_url")
    private String avatarUrl;

    @TableField(value = "content")
    private String content;

    @TableField(value = "website")
    private String website;

    @TableField(value = "is_admin")
    private Integer isAdmin;

    @TableField(value = "status")
    private Integer status;

    @TableField(value = "ip_address")
    private String ipAddress;

    @TableField(value = "user_agent")
    private String userAgent;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}