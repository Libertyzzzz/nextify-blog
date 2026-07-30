package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationListItemVo {
    private String conversationId;
    private String title;
    private String lastMessagePreview;
    private Integer messageCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
