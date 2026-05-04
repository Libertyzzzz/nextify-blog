package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.entity.BlogArticleTag;

public interface BlogArticleTagService extends IService<BlogArticleTag> {

    Page<BlogArticleTag> getArticleTags(long pageNum, long pageSize);
}
