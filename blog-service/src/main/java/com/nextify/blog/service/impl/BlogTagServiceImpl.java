package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.dto.BlogTagAddRequest;
import com.nextify.blog.dto.BlogTagUpdateRequest;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.mapper.BlogTagMapper;
import com.nextify.blog.service.BlogTagService;
import com.nextify.blog.vo.BlogTagVo;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils; // 导入 BeanUtils
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class BlogTagServiceImpl extends ServiceImpl<BlogTagMapper, BlogTag> implements BlogTagService {

    @Resource
    private BlogTagMapper tagMapper; // 尽管使用了ServiceImpl，但保留mapper注入以防其他地方需要

    @Override
    public Page<BlogTagVo> geAllTags(long pageNum, long pageSize) {
        Page<BlogTag> page = new Page<>(pageNum, pageSize);
        Page<BlogTag> tagPage = this.page(page, new LambdaQueryWrapper<BlogTag>()
                .orderByDesc(BlogTag::getCreateTime));
        List<BlogTagVo> vos = tagPage.getRecords().stream().map(tag -> {
            BlogTagVo curr = new BlogTagVo();
            curr.setId(tag.getId()); // 补充ID
            curr.setName(tag.getName());
            curr.setColor(tag.getColor());
            return curr;
        }).toList();
        Page<BlogTagVo> res = new Page<>(pageNum, pageSize, tagPage.getTotal());
        res.setRecords(vos);
        return res;
    }

    @Override
    public List<BlogTagVo> getTagInfos(List<Long> ids) {
       if(ids == null || ids.isEmpty())
           return new ArrayList<>();
        LambdaQueryWrapper<BlogTag> wrapper = new LambdaQueryWrapper<BlogTag>();
        wrapper.in(BlogTag::getId, ids);
        List<BlogTag> res = tagMapper.selectList(wrapper);

        return res.stream().map(tag -> {
            BlogTagVo curr = new BlogTagVo();
            curr.setId(tag.getId()); // 补充ID
            curr.setName(tag.getName());
            curr.setColor(tag.getColor());
            return curr;
        }).collect(Collectors.toList());
    }

    @Override
    public Long addTag(BlogTagAddRequest request) {
        BlogTag tag = new BlogTag();
        BeanUtils.copyProperties(request, tag);
        this.save(tag);
        return tag.getId();
    }

    @Override
    public void updateTag(Long id, BlogTagUpdateRequest request) {
        BlogTag existingTag = this.getById(id);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        BlogTag tag = new BlogTag();
        BeanUtils.copyProperties(request, tag);
        tag.setId(id);
        this.updateById(tag);
    }

    @Override
    public void deleteTag(Long id) {
        BlogTag existingTag = this.getById(id);
        if (existingTag == null) {
            throw new BusinessException("标签不存在");
        }
        // TODO: 考虑标签下是否有文章关联。如果有，需要处理文章的标签归属问题
        // 例如：将关联文章的该标签移除，或者不允许删除有文章关联的标签
        this.removeById(id);
    }
}
