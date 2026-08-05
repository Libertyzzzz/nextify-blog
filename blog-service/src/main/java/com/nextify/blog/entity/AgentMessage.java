package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("agent_message")
public class AgentMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("message_id")
    private String messageId;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("user_id")
    private Long userId;

    @TableField("role")
    private String role; // ENUM('system','user','assistant','tool')

    @TableField("action")
    private String action;

    @TableField("content")
    private String content;

    @TableField("candidates")
    private String candidates; // JSON

    @TableField("context_payload")
    private String contextPayload; // JSON

    @TableField("prompt_tokens")
    private Integer promptTokens;

    @TableField("completion_tokens")
    private Integer completionTokens;

    @TableField("total_tokens")
    private Integer totalTokens;

    @TableField("latency_ms")
    private Integer latencyMs;

    @TableField("model")
    private String model;

    @TableField("status")
    private Integer status;

    @TableField("error_reason")
    private String errorReason;

    @TableField("ip_address")
    private String ipAddress;

    @TableField("user_agent")
    private String userAgent;

    @TableField("create_time")
    private LocalDateTime createTime;
}
