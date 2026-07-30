package com.nextify.blog.service;

import com.nextify.blog.dto.ConversationCreateRequest;
import com.nextify.blog.vo.ConversationVo;

public interface AgentConversationService {
    ConversationVo createConversation(ConversationCreateRequest request);
}
