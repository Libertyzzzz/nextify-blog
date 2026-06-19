package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.dto.BlogTagAddRequest;
import com.nextify.blog.dto.BlogTagUpdateRequest;
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogArticleTag;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.vo.BlogTagVo;

import java.util.List;

public interface BlogTagService extends IService<BlogTag> {
    Page<BlogTagVo> geAllTags(long pageNum, long pageSize);

    List<BlogTagVo> getTagInfos(List<Long> ids);

    /**
     * 新增标签
     * @param request 新增标签请求
     * @return 新增标签的ID
     */
    Long addTag(BlogTagAddRequest request);

    /**
     * 更新标签
     * @param id 标签ID
     * @param request 更新标签请求
     */
    void updateTag(Long id, BlogTagUpdateRequest request);

    /**
     * 删除标签
     * @param id 标签ID
     */
    void deleteTag(Long id);
}
