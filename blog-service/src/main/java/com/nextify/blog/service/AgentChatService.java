package com.nextify.blog.service;

import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.vo.AIChatVo;
import com.nextify.blog.vo.AgentChatResponseVo;

public interface AgentChatService {
    AIChatVo chat(String conversationId, AIChatDto request);
}
