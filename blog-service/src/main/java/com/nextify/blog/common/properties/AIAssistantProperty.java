package com.nextify.blog.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nextify.ai-assistant")
public class AIAssistantProperty {

    private String apiKey;
    private String baseUrl;
    private String model;
    private String embeddingModel;
}
