package com.nextify.blog.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long parentId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private String website;
    private Integer isAdmin;
    private LocalDateTime createTime;
    
    /**
     * 回复列表
     */
    private List<CommentVO> children;
    
    /**
     * 被回复人的昵称 (用于二级评论显示 @xxx)
     */
    private String parentNickname;
}