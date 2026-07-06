package com.nextify.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ImageReferenceDto {
    @NotEmpty(message = "Picture is can not be empty")
    private String imageId;
}
