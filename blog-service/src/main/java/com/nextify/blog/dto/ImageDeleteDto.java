package com.nextify.blog.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ImageDeleteDto {
    @NotEmpty(message = "Picture ids can not be empty")
    private List<Long> ids;
}
