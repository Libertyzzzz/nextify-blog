package com.nextify.blog.service.impl;

import com.nextify.blog.common.third.AliCloudComponent;
import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.service.AgentChatService;
import com.nextify.blog.vo.AIChatVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AgentChatServiceImpl implements AgentChatService {
    @Resource
    private AliCloudComponent aliCloudComponent;
    @Override
    public AIChatVo chat(AIChatDto request) {

        return aliCloudComponent.callWithMessage(request);
    }
}
