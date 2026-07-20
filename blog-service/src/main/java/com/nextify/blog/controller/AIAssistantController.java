package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.service.AgentChatService;
import com.nextify.blog.vo.AIChatVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent")
public class AIAssistantController {

    @Resource
    private AgentChatService agentChatService;
    @PostMapping("/chat")
    public Result<AIChatVo> chat(@RequestBody AIChatDto request){
        return Result.success(agentChatService.chat(request));
    }

}
