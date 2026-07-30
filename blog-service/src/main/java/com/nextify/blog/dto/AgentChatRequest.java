package com.nextify.blog.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AgentChatRequest {
    private String message;
    private String action;
    private Context context;
    private Boolean stream;

    @Data
    public static class Context {
        private String key;
        private Map<String, Object> payload;
    }
}
