package com.nextify.blog.service;

import com.nextify.blog.dto.LlmRequest;
import com.nextify.blog.vo.LlmResponse;
import reactor.core.publisher.Flux; // For streaming

public interface LlmProvider {
    String getProviderName();
    LlmResponse chat(LlmRequest request);
    Flux<String> chatStream(LlmRequest request); // Optional for streaming
}
