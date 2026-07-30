package com.nextify.blog.vo;

import lombok.Data;

import java.util.List;

@Data
public class LlmResponse {
    private String content;
    private List<String> candidates;
    private UsageVo usage;
    private String model;
    private String provider;
    private Integer latencyMs;
    private String messageId; // Optional, if provider returns one
    private String finishReason; // e.g., stop, length, content_filter
}
