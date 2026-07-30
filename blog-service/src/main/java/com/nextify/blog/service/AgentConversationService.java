package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.ConversationCreateRequest;
import com.nextify.blog.vo.ConversationListItemVo;
import com.nextify.blog.vo.ConversationVo;

public interface AgentConversationService {
    ConversationVo createConversation(ConversationCreateRequest request);
    Page<ConversationListItemVo> getConversationList(Long userId, int page, int pageSize);
}
