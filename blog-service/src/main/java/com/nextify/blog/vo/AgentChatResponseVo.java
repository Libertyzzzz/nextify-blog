package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentChatResponseVo {
    private String messageId;
    private String role;
    private String content;
    private List<String> candidates;
    private String action;
    private UsageVo usage;
    private LocalDateTime createdAt;
}
