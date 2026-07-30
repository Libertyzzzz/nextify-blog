package com.nextify.blog.dto;

import lombok.Data;

@Data
public class ConversationCreateRequest {
    private String title;
    private String contextKey;
}
