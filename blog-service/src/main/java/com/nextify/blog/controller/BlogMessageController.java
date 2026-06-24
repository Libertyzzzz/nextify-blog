package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.entity.BlogMessage;
import com.nextify.blog.service.impl.BlogMessageServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
public class BlogMessageController {

    @Autowired
    private BlogMessageServiceImpl messageService;

    /**
     * 发送私信
     */
    @PostMapping("/send")
    public Result<Void> send(@RequestBody BlogMessage message) {
        messageService.sendMessage(message);
        return Result.success();
    }

    /**
     * 获取未读私信
     */
    @GetMapping("/unread/{anonymousId}")
    public Result<List<BlogMessage>> getUnread(@PathVariable String anonymousId) {
        return Result.success(messageService.getUnreadMessages(anonymousId));
    }

    @PutMapping("/read/{id}")
    public Result<Void> read(@PathVariable Long id) {
        messageService.markAsRead(id);
        return Result.success();
    }
}