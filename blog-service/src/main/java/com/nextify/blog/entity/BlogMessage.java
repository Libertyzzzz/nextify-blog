package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("blog_message")
public class BlogMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String fromId;

    private String toId;

    private String content;

    private Integer type;

    private Integer isRead;

    private LocalDateTime createTime;
}