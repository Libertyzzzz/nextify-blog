package com.nextify.blog.vo;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Setter
@Getter
public class AgentMessageVo {


    private String conversationId;
    private String content;
    private String role;
    private LocalDateTime createTime;
}
