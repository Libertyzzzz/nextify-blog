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
@TableName("agent_usage_log")
public class AgentUsageLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("conversation_id")
    private String conversationId;

    @TableField("message_id")
    private String messageId;

    @TableField("action")
    private String action;

    @TableField("context_key")
    private String contextKey;

    @TableField("model")
    private String model;

    @TableField("provider")
    private String provider;

    @TableField("prompt_tokens")
    private Integer promptTokens;

    @TableField("completion_tokens")
    private Integer completionTokens;

    @TableField("total_tokens")
    private Integer totalTokens;

    @TableField("latency_ms")
    private Integer latencyMs;

    @TableField("status")
    private Integer status;

    @TableField("error_code")
    private String errorCode;

    @TableField("request_at")
    private LocalDateTime requestAt;
}
