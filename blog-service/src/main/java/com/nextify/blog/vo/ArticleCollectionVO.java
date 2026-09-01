package com.nextify.blog.vo;

import java.time.LocalDateTime;
import java.util.List;

public class ArticleCollectionVO {

    private Long id;
    private String title;
    private String subtitle;
    private String summary;

    private String coverImg;
    private Integer cardStyle;
    private Integer viewCount;
    private Integer isTop;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long categoryId;

    private List<String> tagNames;
}
