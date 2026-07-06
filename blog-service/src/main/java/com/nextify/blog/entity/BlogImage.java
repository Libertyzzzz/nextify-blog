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

    /** 文件MD5值，用于秒传和完整性校验 */
    private String md5;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 图片Alt文本，用于SEO和可访问性 */
    @TableField("alt_text")
    private String altText;

    /** 图片标题 */
    private String title;

    /** 引用次数 */
    @TableField("reference_count")
    private Integer referenceCount;

    /** 使用类型 */
    @TableField("usage_type")
    private Integer usageType;

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
    @TableLogic(value = "null", delval = "now")
    @TableField("delete_time")
    private LocalDateTime deleteTime;
}