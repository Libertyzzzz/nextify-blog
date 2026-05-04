package com.nextify.blog.vo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ArticleTagDetailVO {

    private Long articleId;
    private List<BlogTagVo> tagIds;

}
