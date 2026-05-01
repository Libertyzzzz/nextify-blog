package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文章与标签多对多关联实体类
 * 对应表：blog_article_tag
 */
@Data
@TableName("blog_article_tag")
public class BlogArticleTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("article_id")
    private Long articleId;

    @TableField("tag_id")
    private Long tagId;
}
