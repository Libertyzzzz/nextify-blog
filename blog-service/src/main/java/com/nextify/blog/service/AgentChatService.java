package com.nextify.blog.service;

import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.vo.AIChatVo;

public interface AgentChatService {
    /**
     * 处理AI聊天请求
     *
     * @param request 聊天请求
     * @return 聊天响应
     */
    AIChatVo chat(AIChatDto request);
}
