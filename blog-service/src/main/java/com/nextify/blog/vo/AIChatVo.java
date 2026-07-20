package com.nextify.blog.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AIChatVo {
    /**
     * AI回复内容
     */
    private String content;

    /**
     * 候选答案列表
     */
    private List<String> candidates;

    /**
     * 动作类型
     */
    private String action;
}
