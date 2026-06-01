package com.nextify.blog.enums;

public enum ImageUsageType {
    ARTICLE_COVER("文章封面"),
    AVATAR("用户头像"),
    ARTICLE_CONTENT("文章内容"),
    OTHER("其他用途");

    private final String description;

    ImageUsageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}