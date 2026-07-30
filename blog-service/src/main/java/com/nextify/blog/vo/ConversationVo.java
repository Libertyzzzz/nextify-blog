package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVo {
    private String conversationId;
    private String title;
    private String contextKey;
    private LocalDateTime createdAt;
}
