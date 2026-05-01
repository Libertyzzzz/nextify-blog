package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章标签实体类
 * 对应表：blog_tag
 */
@Data
@TableName("blog_tag")
public class BlogTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称 */
    private String name;

    /** 标签颜色 (十六进制) */
    private String color;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
