package com.nextify.blog.enums;

public enum ImageUsageType {
    ARTICLE_COVER("文章封面", 1),
    AVATAR("用户头像", 2),
    ARTICLE_CONTENT("文章内容", 3),
    EVALUATION_IMAGE("评估图像", 4),
    OTHER("其他用途", 100);

    private final String description;
    private final  Integer type;

    ImageUsageType(String description, Integer type) {
        this.type = type;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}