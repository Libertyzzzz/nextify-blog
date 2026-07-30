package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.mapper.BlogArticleMapper;
import com.nextify.blog.service.ContentService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ContentServiceImpl implements ContentService {

    @Resource
    private BlogArticleMapper blogArticleMapper;

    @Override
    public Map<String, Object> getArticleContext(String articleId) {
        // 假设 articleId 是 BlogArticle 的 articleId 字段
        BlogArticle article = blogArticleMapper.selectOne(
                new QueryWrapper<BlogArticle>().eq("article_id", articleId)
        );

        if (article != null) {
            Map<String, Object> context = new HashMap<>();
            context.put("articleTitle", article.getTitle());
            // 截取文章内容的前一部分作为摘要，避免过长
            String contentSummary = article.getContent().length() > 500 ?
                                    article.getContent().substring(0, 500) + "..." :
                                    article.getContent();
            context.put("articleContentSummary", contentSummary);
            // TODO: 可以根据需要添加更多文章信息，例如标签、分类等
            // context.put("articleTags", getArticleTags(article.getId()));
            // context.put("articleCategory", getArticleCategory(article.getCategoryId()));
            return context;
        }
        return null;
    }
}
