package com.nextify.blog.dto;

import lombok.Data;

/**
 * 图片查询参数对象
 */
@Data
public class ImageQueryDto {

    /**
     * 搜索关键字（模糊匹配原始文件名或存储文件名）
     */
    private String keyword;

    /**
     * 使用类型（如：ARTICLE, AVATAR, AD）
     */
    private String usageType;

    /**
     * 是否为临时文件
     */
    private Boolean isTemporary;

    /**
     * 文件MIME类型筛选（如：image/webp）
     */
    private String mimeType;

    /**
     * 分页页码
     */
    private Long pageNum = 1L;

    /**
     * 每页数量
     */
    private Long pageSize = 10L;
}
