package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.BlogArticleTag;
import com.nextify.blog.mapper.BlogArticleTagMapper;
import com.nextify.blog.service.BlogArticleTagService;
import org.springframework.stereotype.Service;

@Service
public class BlogArticleTagServiceImpl extends ServiceImpl<BlogArticleTagMapper, BlogArticleTag> implements BlogArticleTagService {



    @Override
    public Page<BlogArticleTag> getArticleTags(long pageNum, long pageSize) {
        return null;
    }
}
