package com.nextify.blog.dto;

import lombok.Data;

@Data
public class SensitiveWordQueryDto {

    private int pageSize = 10;
    private int pageNum = 1;
    private String prefix;
}
