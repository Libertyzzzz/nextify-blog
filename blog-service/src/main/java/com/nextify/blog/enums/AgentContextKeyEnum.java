package com.nextify.blog.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum AgentContextKeyEnum {
    /**
     * Home page Hero section
     */
    HOME("home"),
    PUBLISH("publish"),
    POST_DETAIL("post-detail"),
    GUESTBOOK("guestbook"),
    PROFILE("profile"),
    ASSESSMENT("assessment");

    @JsonValue
    private final String key;

    AgentContextKeyEnum(String key) {
        this.key = key;
    }


}