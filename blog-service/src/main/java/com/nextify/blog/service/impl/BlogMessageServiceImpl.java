package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.BlogMessage;
import com.nextify.blog.mapper.BlogMessageMapper;
import com.nextify.blog.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogMessageServiceImpl extends ServiceImpl<BlogMessageMapper, BlogMessage> {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 发送私信
     */
    public void sendMessage(BlogMessage message) {
        // 脱敏处理
        String cleanContent = sensitiveWordFilter.replaceSensitiveWord(message.getContent());
        message.setContent(cleanContent);
        message.setIsRead(0);
        this.save(message);

        // TODO: 这里可以调用 SSE 推送通知给 toId
    }

    /**
     * 获取我的未读消息
     */
    public List<BlogMessage> getUnreadMessages(String anonymousId) {
        return this.list(new LambdaQueryWrapper<BlogMessage>()
                .eq(BlogMessage::getToId, anonymousId)
                .eq(BlogMessage::getIsRead, 0)
                .orderByDesc(BlogMessage::getCreateTime));
    }

    public void markAsRead(Long messageId) {
        BlogMessage msg = new BlogMessage();
        msg.setId(messageId);
        msg.setIsRead(1);
        this.updateById(msg);
    }
}