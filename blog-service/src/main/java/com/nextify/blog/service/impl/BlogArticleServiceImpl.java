package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.ArticlePublishRequest;
import com.nextify.blog.entity.BlogArticle;
import com.nextify.blog.entity.BlogArticleTag;
import com.nextify.blog.entity.BlogCategory;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.mapper.BlogArticleMapper;
import com.nextify.blog.mapper.BlogArticleTagMapper;
import com.nextify.blog.mapper.BlogCategoryMapper;
import com.nextify.blog.mapper.BlogTagMapper;
import com.nextify.blog.service.BlogArticleService;
import com.nextify.blog.vo.ArticleDetailVO;
import com.nextify.blog.vo.ArticleListItemVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BlogArticleServiceImpl extends ServiceImpl<BlogArticleMapper, BlogArticle> implements BlogArticleService {

    @Autowired
    private BlogArticleTagMapper articleTagMapper;
    @Autowired
    private BlogCategoryMapper categoryMapper;
    @Autowired
    private BlogTagMapper tagMapper;

    @Override
    public Page<ArticleListItemVO> getHomeArticles(long pageNum, long pageSize) {
        Page<BlogArticle> page = new Page<>(pageNum, pageSize);
        Page<BlogArticle> articlePage = this.page(page, new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, 1)
                .orderByDesc(BlogArticle::getIsTop)
                .orderByDesc(BlogArticle::getCreateTime));
        Map<Long, String> categoryMap = buildCategoryMap(articlePage.getRecords());
        Map<Long, List<String>> articleTags = buildArticleTagNameMap(articlePage.getRecords());
        List<ArticleListItemVO> voList = articlePage.getRecords().stream().map(article -> {
            ArticleListItemVO vo = new ArticleListItemVO();
            vo.setId(article.getId());
            vo.setTitle(article.getTitle());
            vo.setSubtitle(article.getSubtitle());
            vo.setSummary(article.getSummary());
            vo.setCoverImg(article.getCoverImg());
            vo.setCardStyle(article.getCardStyle());
            vo.setViewCount(article.getViewCount());
            vo.setIsTop(article.getIsTop());
            vo.setCreateTime(article.getCreateTime());
            vo.setCategoryId(article.getCategoryId());
            vo.setCategoryName(categoryMap.get(article.getCategoryId()));
            vo.setTagNames(articleTags.getOrDefault(article.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());

        Page<ArticleListItemVO> result = new Page<>(pageNum, pageSize, articlePage.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    @Transactional
    public ArticleDetailVO getArticleDetail(Long id) {
        // 增加阅读量
        baseMapper.incrementViewCount(id);
        BlogArticle article = this.getOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getId, id)
                .eq(BlogArticle::getStatus, 1));
        if (article == null) {
            return null;
        }
        Map<Long, String> categoryMap = buildCategoryMap(List.of(article));
        Map<Long, List<String>> articleTags = buildArticleTagNameMap(List.of(article));

        ArticleDetailVO vo = new ArticleDetailVO();
        BeanUtils.copyProperties(article, vo);
        vo.setRenderContent(StringUtils.hasText(article.getContentHtml()) ? article.getContentHtml() : article.getContent());
        vo.setCategoryName(categoryMap.get(article.getCategoryId()));
        vo.setTagNames(articleTags.getOrDefault(article.getId(), new ArrayList<>()));
        return vo;
    }

    @Override
    @Transactional
    public Long publishArticle(ArticlePublishRequest request) {
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(request, article);
        if (article.getViewCount() == null) {
            article.setViewCount(0);
        }
        if (article.getIsTop() == null) {
            article.setIsTop(0);
        }
        this.save(article);

        articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", article.getId()));
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                BlogArticleTag rel = new BlogArticleTag();
                rel.setArticleId(article.getId());
                rel.setTagId(tagId);
                articleTagMapper.insert(rel);
            }
        }
        return article.getId();
    }

    @Override
    @Transactional
    public Long updateArticle(Long id, ArticlePublishRequest request) {
        BlogArticle existing = this.getById(id);
        if (existing == null) {
            throw new com.nextify.blog.common.exception.BusinessException("文章不存在");
        }
        BlogArticle article = new BlogArticle();
        BeanUtils.copyProperties(request, article);
        article.setId(id);
        if (article.getViewCount() == null) {
            article.setViewCount(existing.getViewCount() == null ? 0 : existing.getViewCount());
        }
        this.updateById(article);

        articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", id));
        if (request.getTagIds() != null) {
            for (Long tagId : request.getTagIds()) {
                BlogArticleTag rel = new BlogArticleTag();
                rel.setArticleId(id);
                rel.setTagId(tagId);
                articleTagMapper.insert(rel);
            }
        }
        return id;
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        BlogArticle article = this.getById(id);
        if (article == null) {
            throw new com.nextify.blog.common.exception.BusinessException("文章不存在");
        }
        article.setStatus(-1);
        this.updateById(article);
        articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", id));
    }

    private Map<Long, String> buildCategoryMap(List<BlogArticle> articles) {
        List<Long> categoryIds = articles.stream()
                .map(BlogArticle::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        if (categoryIds.isEmpty()) {
            return new HashMap<>();
        }
        return categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(BlogCategory::getId, BlogCategory::getName, (a, b) -> a));
    }

    private Map<Long, List<String>> buildArticleTagNameMap(List<BlogArticle> articles) {
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).collect(Collectors.toList());
        if (articleIds.isEmpty()) {
            return new HashMap<>();
        }
        List<BlogArticleTag> rels = articleTagMapper.selectList(
                new QueryWrapper<BlogArticleTag>().in("article_id", articleIds));
        if (rels.isEmpty()) {
            return new HashMap<>();
        }
        List<Long> tagIds = rels.stream().map(BlogArticleTag::getTagId).distinct().collect(Collectors.toList());
        Map<Long, String> tagNameMap = tagMapper.selectBatchIds(tagIds).stream()
                .collect(Collectors.toMap(BlogTag::getId, BlogTag::getName, (a, b) -> a));

        Map<Long, List<String>> result = new HashMap<>();
        for (BlogArticleTag rel : rels) {
            String tagName = tagNameMap.get(rel.getTagId());
            if (tagName == null) {
                continue;
            }
            result.computeIfAbsent(rel.getArticleId(), k -> new ArrayList<>()).add(tagName);
        }
        return result;
    }
}
