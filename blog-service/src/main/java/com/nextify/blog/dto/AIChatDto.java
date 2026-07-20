package com.nextify.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AIChatDto {
    /**
     * 动作类型：chat/generate_title/polish_text/continue_write/generate_summary/check_typo/generate_outline
     */
    @NotBlank(message = "动作类型不能为空")
    private String action;

    /**
     * 用户输入消息
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 上下文内容（用于提供额外信息）
     */
    private String context;

    /**
     * 标题（用于生成标题等操作）
     */
    private String title;

    /**
     * 风格：humor/academic/minimal/viral/casual
     */
    private String style;

}
