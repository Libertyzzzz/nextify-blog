package com.nextify.blog.service;

import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.entity.AgentConversation;
import com.nextify.blog.vo.AgentChatResponseVo;
import com.nextify.blog.vo.AgentMessageVo;

import java.util.List;

public interface ConversationChatService {
    AgentChatResponseVo chat(String conversationId, AgentChatRequest request);
    List<AgentMessageVo> load(String conversationID);
    Boolean update(String conversationID, String title);
    Boolean delete(String conversationID);
}
