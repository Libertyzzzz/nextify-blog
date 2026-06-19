package com.nextify.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BlogCategoryAddRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;
    private String icon;
    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
