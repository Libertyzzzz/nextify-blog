package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("blog_image")
public class BlogImage {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 存储的文件名（UUID） */
    @TableField("file_name")
    private String fileName;

    /** 原始文件名 */
    @TableField("original_name")
    private String originalName;

    /** 相对路径 */
    private String path;

    /** 完整访问URL */
    private String url;

    /** 文件大小（字节） */
    private Long size;

    /** MIME类型 */
    @TableField("mime_type")
    private String mimeType;

    /** 使用类型 */
    @TableField("usage_type")
    private String usageType;

    /** 关联业务ID */
    @TableField("usage_id")
    private Long usageId;

    /** 是否为临时图片（未与业务实体关联） */
    @TableField("is_temporary")
    private Boolean isTemporary;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 软删除时间 */
    @TableField("deleted_time")
    private LocalDateTime deleteTime;
}