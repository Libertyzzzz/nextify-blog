package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 博客文章实体类
 * 对应表：blog_article
 */
@Data
@TableName("blog_article")
public class BlogArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;
    private String subtitle;
    private String summary;
    private String content;

    @TableField("content_html")
    private String contentHtml;

    @TableField("cover_img")
    private String coverImg;

    @TableField("card_style")
    private Integer cardStyle;

    private Integer status;

    @TableField("view_count")
    private Integer viewCount;

    @TableField("is_top")
    private Integer isTop;

    @TableField("category_id")
    private Long categoryId;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
