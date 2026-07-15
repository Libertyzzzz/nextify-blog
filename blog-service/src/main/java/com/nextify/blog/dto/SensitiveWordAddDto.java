package com.nextify.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SensitiveWordAddDto {
    @NotEmpty(message = "敏感词不能为空")
    private String word;

    private String category = "政治";
}
