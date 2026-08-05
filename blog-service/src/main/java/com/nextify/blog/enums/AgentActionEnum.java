package com.nextify.blog.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AgentActionEnum {
    /**
     * Default chat
     */
    CHAT("chat"),
    GENERATE_TITLE("generate_title"),
    POLISH_TEXT("polish_text"),
    CONTINUE_WRITE("continue_write"),
    GENERATE_SUMMARY("generate_summary"),
    CHECK_TYPO("check_typo"),
    GENERATE_OUTLINE("generate_outline");

    @JsonValue
    private final String action;

    AgentActionEnum(String action) {
        this.action = action;
    }


}