package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailVO {
    private Long id;
    private String title;
    private String subtitle;
    private String summary;
    private String content;
    private String contentHtml;
    private String renderContent;
    private String coverImg;
    private Integer cardStyle;
    private Integer viewCount;
    private Integer isTop;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long categoryId;
    private String categoryName;
    private List<String> tagNames;
}
