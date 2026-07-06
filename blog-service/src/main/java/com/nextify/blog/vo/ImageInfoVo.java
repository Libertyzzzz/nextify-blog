package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 图片信息展示对象
 */
@Data
public class ImageInfoVo {

    private Long id;

    /** 存储的文件名 */
    private String fileName;

    /** 原始文件名 */
    private String originalName;

    /** 完整访问URL */
    private String url;

    /** 文件大小（字节） */
    private Long size;

    /** MIME类型 */
    private String mimeType;

    /** 图片宽度 */
    private Integer width;

    /** 图片高度 */
    private Integer height;

    /** 图片Alt文本 */
    private String altText;

    /** 图片标题 */
    private String title;

    /** 引用次数 */
    private Integer referenceCount;

    /** 使用类型（文章、头像等） */
    private String usageType;

    /** 关联业务ID */
    private Long usageId;

    /** 是否为临时图片 */
    private Boolean isTemporary;

    /** 上传时间 */
    private LocalDateTime createTime;
}
