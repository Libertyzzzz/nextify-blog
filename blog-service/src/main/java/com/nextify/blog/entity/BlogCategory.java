package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文章分类实体类
 * 对应表：blog_category
 */
@Data
@TableName("blog_category")
public class BlogCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
    private String icon;
    private Integer sort;
}