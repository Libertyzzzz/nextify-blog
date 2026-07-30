package com.nextify.blog.dto;

import lombok.Data;

import java.util.List;

@Data
public class LlmRequest {
    private List<LlmMessage> messages;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private String action; // For routing or specific provider logic
    // Add other common LLM parameters as needed
}
