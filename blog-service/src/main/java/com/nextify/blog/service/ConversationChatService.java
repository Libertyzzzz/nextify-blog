package com.nextify.blog.service;

import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.vo.AgentChatResponseVo;

public interface ConversationChatService {
    AgentChatResponseVo chat(String conversationId, AgentChatRequest request);
}
