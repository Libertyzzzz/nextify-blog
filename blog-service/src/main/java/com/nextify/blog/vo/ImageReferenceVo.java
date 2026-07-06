package com.nextify.blog.vo;

import lombok.Data;

@Data
public class ImageReferenceVo {
    private Integer usageType;
    private Long usageId;
    // 引用源的标题 如果是文章
    private String sourceTitle;
    // 预览链接（可选）
    private String sourceUrl;
}