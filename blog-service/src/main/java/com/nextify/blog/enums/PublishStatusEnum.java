package com.nextify.blog.enums;

import lombok.Getter;

@Getter
public enum PublishStatusEnum {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布"),
    PRIVATE(2, "私密");

    private final Integer code;
    private final String desc;

    PublishStatusEnum(Integer code, String desc){
        this.code = code;
        this.desc = desc;
    }

}
