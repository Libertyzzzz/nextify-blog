package com.nextify.blog.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RateLimiter; // 导入 RateLimiter
import com.nextify.blog.common.annotaion.RequiredLogin; // 导入 RequiredLogin
import com.nextify.blog.common.context.UserContextHolder;
import com.nextify.blog.dto.AIChatDto;
import com.nextify.blog.dto.AgentChatRequest;
import com.nextify.blog.dto.ConversationCreateRequest;
import com.nextify.blog.service.AgentChatService;
import com.nextify.blog.service.AgentConversationService;
import com.nextify.blog.service.ConversationChatService;
import com.nextify.blog.vo.AIChatVo;
import com.nextify.blog.vo.AgentChatResponseVo;
import com.nextify.blog.vo.ConversationListItemVo;
import com.nextify.blog.vo.ConversationVo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AIAssistantController {

    @Resource
    private AgentChatService agentChatService;

    @Resource
    private AgentConversationService agentConversationService;

    @Resource
    private ConversationChatService conversationChatService;

    /**
     * 通用接口 无状态
     * @param request
     * @param conversationId
     * @return
     */
    @RequiredLogin // 需要登录
    @RateLimiter(time = 5, count = 2)
    @PostMapping("/chat")
    public Result<AIChatVo> chat(@RequestBody AIChatDto request,
                                 @RequestBody(required = false) String conversationId){
        // 保持原有业务逻辑不变
        return Result.success(agentChatService.chat(conversationId, request));
    }

    @RequiredLogin
    @RateLimiter(time = 10, count = 5)
    @PostMapping("/conversations")
    public Result<ConversationVo> createConversation(@RequestBody ConversationCreateRequest request) {
        return Result.success(agentConversationService.createConversation(request));
    }

    @RequiredLogin
    @RateLimiter(time = 10, count = 10)
    @GetMapping("/conversations")
    public Result<Page<ConversationListItemVo>> getConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        Long userId = UserContextHolder.getUserId();
        return Result.success(agentConversationService.getConversationList(userId, page, pageSize));
    }

    @RequiredLogin
    @RateLimiter(time = 5, count = 2)
    @PostMapping("/chat/{conversationId}")
    public Result<AgentChatResponseVo> chatWithConversation(
            @PathVariable String conversationId,
            @RequestBody AgentChatRequest request) {
        return Result.success(conversationChatService.chat(conversationId, request));
    }
}
