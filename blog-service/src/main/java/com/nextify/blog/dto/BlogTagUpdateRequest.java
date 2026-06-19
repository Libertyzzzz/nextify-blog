package com.nextify.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BlogTagUpdateRequest {
    @NotBlank(message = "标签名称不能为空")
    private String name;
    private String color; // 标签颜色 (十六进制)
}
