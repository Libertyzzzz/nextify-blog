package com.nextify.blog.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum LlmProviderEnum {

    ALIYUN("aliyun"),
    OPENAI("openai"),
    DEEPSEEK("deepseek");

    @JsonValue
    private final String providerName;

    LlmProviderEnum(String providerName) {
        this.providerName = providerName;
    }
}