package com.nextify.blog.vo;

import lombok.Data;

@Data
public class UsageVo {
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
}
