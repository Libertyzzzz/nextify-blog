package com.nextify.blog.vo;

import lombok.Data;

@Data
public class CategoryVo {
    private Long id;

    private String name;
    private String icon;
    private Integer sort;
}
