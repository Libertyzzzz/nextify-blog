package com.nextify.blog.enums;

import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
public enum CommentStatusEnum {
    PENDING_REVIEW(0, "待审核"),
    PUBLISHED(1, "已发布"),
    DELETED(2, "已删除"),
    SPAM(3, "垃圾评论"),
    REJECTED(4, "审核未通过");

    private final Integer code;
    private final String description;

    CommentStatusEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}