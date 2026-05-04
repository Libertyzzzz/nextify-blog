package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogArticleTag;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.vo.BlogTagVo;

import java.util.List;

public interface BlogTagService extends IService<BlogTag> {
    Page<BlogTagVo> geAllTags(long pageNum, long pageSize);

    List<BlogTagVo> getTagInfos(List<Long> ids);
}
