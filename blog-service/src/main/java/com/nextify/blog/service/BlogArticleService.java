package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.dto.ArticlePublishRequest;
import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.vo.ArticleDetailVO;
import com.nextify.blog.vo.ArticleListItemVO;

public interface BlogArticleService extends IService<BlogArticle> {

    Page<ArticleListItemVO> getHomeArticles(long pageNum, long pageSize);

    ArticleDetailVO getArticleDetail(Long id);

    Long publishArticle(ArticlePublishRequest request);

    Long updateArticle(Long id, ArticlePublishRequest request);

    void deleteArticle(Long id);
}
