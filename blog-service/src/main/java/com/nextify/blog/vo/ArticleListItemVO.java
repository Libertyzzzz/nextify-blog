package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListItemVO {
    private Long id;
    private String title;
    private String subtitle;
    private String summary;
    private String coverImg;
    private Integer cardStyle;
    private Integer viewCount;
    private Integer isTop;
    private LocalDateTime createTime;
    private Long categoryId;
    private String categoryName;
    private List<String> tagNames;
}
