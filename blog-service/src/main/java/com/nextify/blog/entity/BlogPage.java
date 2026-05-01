package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 自定义页面实体类 (如：关于我、留言板等)
 * 对应表：blog_page
 */
@Data
@TableName("blog_page")
public class BlogPage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 页面标题 */
    private String title;

    /** 页面别名 (URL路径使用，如 about) */
    @TableField("page_label")
    private String pageLabel;

    /** 页面内容 (Markdown/HTML) */
    private String content;

    /** 封面图 */
    @TableField("cover_img")
    private String coverImg;

    /** 是否开启评论：0-否，1-是 */
    @TableField("can_comment")
    private Integer canComment;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}