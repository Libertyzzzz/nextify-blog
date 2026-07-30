package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("agent_message")
public class AgentMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String messageId;

    private String conversationId;

    private Long userId;

    private String role;

    private String content;

    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;

    private String model;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}