package com.nextify.blog.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LlmModelEnum {

    QWEN_PLUS("qwen-plus"),
    QWEN_TURBO("qwen-turbo"),
    GPT_4_O("gpt-4o"),
    DEEPSEEK_CHAT("deepseek-chat");

    @JsonValue
    private final String modelName;

    LlmModelEnum(String modelName) {
        this.modelName = modelName;
    }
}