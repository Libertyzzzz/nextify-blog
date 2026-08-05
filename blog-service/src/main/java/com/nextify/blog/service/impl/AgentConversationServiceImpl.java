package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.ConversationCreateRequest;
import com.nextify.blog.entity.AgentConversation;
import com.nextify.blog.mapper.AgentConversationMapper;
import com.nextify.blog.service.AgentConversationService;
import com.nextify.blog.vo.ConversationListItemVo;
import com.nextify.blog.vo.ConversationVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {
    private static final String CONVERSATION_PREFIX = "conv_";
    private static final String DEFAULT_TITLE = "新会话";

    @Resource
    private AgentConversationMapper agentConversationMapper;

    @Override
    public ConversationVo createConversation(ConversationCreateRequest request) {
        AgentConversation conversation = new AgentConversation();
        conversation.setConversationId(CONVERSATION_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 16)); // 生成一个 conv_ 开头的唯一ID
        // 从上下文获取当前登录用户ID
        conversation.setUserId(UserContextHolder.getUserId());
        conversation.setTitle(request.getTitle() != null && !request.getTitle().isEmpty() ? request.getTitle() : DEFAULT_TITLE);
        conversation.setContextKey(request.getContextKey() != null && !request.getContextKey().isEmpty() ? request.getContextKey() : "generic");
        conversation.setStatus(1); // 默认活跃
        conversation.setMessageCount(0);


        agentConversationMapper.insert(conversation);

        ConversationVo vo = new ConversationVo();
        vo.setConversationId(conversation.getConversationId());
        vo.setTitle(conversation.getTitle());
        vo.setContextKey(conversation.getContextKey());
        vo.setCreatedAt(conversation.getCreateTime());
        return vo;
    }

    @Override
    public Page<ConversationListItemVo> getConversationList(Long userId, int page, int pageSize) {
        Page<AgentConversation> mpPage = new Page<>(page, pageSize);
        QueryWrapper<AgentConversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("status", 1); // 只查询活跃的会话
        queryWrapper.orderByDesc("create_time");

        Page<AgentConversation> conversationPage = agentConversationMapper.selectPage(mpPage, queryWrapper);

        List<ConversationListItemVo> voList = conversationPage.getRecords().stream().map(conversation -> {
            ConversationListItemVo vo = new ConversationListItemVo();
            vo.setConversationId(conversation.getConversationId());
            vo.setTitle(conversation.getTitle());
            vo.setLastMessagePreview(conversation.getLastMessagePreview());
            vo.setMessageCount(conversation.getMessageCount());
            vo.setCreatedAt(conversation.getCreateTime());
            vo.setUpdatedAt(conversation.getUpdateTime());
            return vo;
        }).collect(Collectors.toList());

        Page<ConversationListItemVo> resultPage = new Page<>(conversationPage.getCurrent(), conversationPage.getSize(), conversationPage.getTotal());
        resultPage.setRecords(voList);
        return resultPage;
    }
}
