package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 友情链接实体类
 * 对应表：blog_link
 */
@Data
@TableName("blog_link")
public class BlogLink {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 网站名称 */
    private String name;

    /** 网站链接 */
    private String url;

    /** 网站Logo/头像 */
    private String avatar;

    /** 网站简介 */
    private String description;

    /** 排序权重 */
    private Integer sort;

    /** 状态：0-禁用，1-启用 */
    private Integer status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}