package com.nextify.blog.service.impl;

import com.nextify.blog.dto.ConversationCreateRequest;
import com.nextify.blog.entity.AgentConversation;
import com.nextify.blog.mapper.AgentConversationMapper;
import com.nextify.blog.service.AgentConversationService;
import com.nextify.blog.vo.ConversationVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {

    @Resource
    private AgentConversationMapper agentConversationMapper;

    @Override
    public ConversationVo createConversation(ConversationCreateRequest request) {
        AgentConversation conversation = new AgentConversation();
        conversation.setConversationId("conv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)); // 生成一个 conv_ 开头的唯一ID
        // TODO: 从认证信息中获取 userId
        conversation.setUserId(1L); // 暂时硬编码为1L，后续从认证信息中获取
        conversation.setTitle(request.getTitle() != null && !request.getTitle().isEmpty() ? request.getTitle() : "新对话");
        conversation.setContextKey(request.getContextKey() != null && !request.getContextKey().isEmpty() ? request.getContextKey() : "generic");
        conversation.setStatus(1); // 默认活跃
        conversation.setMessageCount(0);
        conversation.setCreatedAt(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());

        agentConversationMapper.insert(conversation);

        ConversationVo vo = new ConversationVo();
        vo.setConversationId(conversation.getConversationId());
        vo.setTitle(conversation.getTitle());
        vo.setContextKey(conversation.getContextKey());
        vo.setCreatedAt(conversation.getCreatedAt());
        return vo;
    }
}
