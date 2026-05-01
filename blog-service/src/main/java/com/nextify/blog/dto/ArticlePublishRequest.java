package com.nextify.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArticlePublishRequest {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String subtitle;
    private String summary;
    @NotBlank(message = "内容不能为空")
    private String content;
    private String contentHtml;
    private String coverImg;
    private Integer cardStyle;
    @NotNull(message = "状态不能为空")
    private Integer status;
    private Integer isTop;
    @NotNull(message = "分类不能为空")
    private Long categoryId;
    private List<Long> tagIds;
}
