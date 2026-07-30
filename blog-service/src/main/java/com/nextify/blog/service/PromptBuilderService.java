package com.nextify.blog.service;

import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.dto.LlmMessage;

import java.util.List;

public interface PromptBuilderService {
    List<LlmMessage> buildPromptMessages(Long userId, String conversationId, AgentChatRequest request);
}
