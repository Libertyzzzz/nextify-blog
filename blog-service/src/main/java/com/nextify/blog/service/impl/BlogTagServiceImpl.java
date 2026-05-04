package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.TagQueryRequest;
import com.nextify.blog.entity.BlogTag;
import com.nextify.blog.mapper.BlogTagMapper;
import com.nextify.blog.service.BlogTagService;
import com.nextify.blog.vo.BlogTagVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class BlogTagServiceImpl extends ServiceImpl<BlogTagMapper, BlogTag> implements BlogTagService {

    @Resource
    private BlogTagMapper tagMapper;
    @Override
    public Page<BlogTagVo> geAllTags(long pageNum, long pageSize) {
        Page<BlogTag> page = new Page<>(pageNum, pageSize);
        Page<BlogTag> tagPage = this.page(page, new LambdaQueryWrapper<BlogTag>()
                .orderByDesc(BlogTag::getCreateTime));
        List<BlogTagVo> vos = tagPage.getRecords().stream().map(tag -> {
            BlogTagVo curr = new BlogTagVo();
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
            curr.setName(tag.getName());
            curr.setColor(tag.getColor());
            return curr;
        }).collect(Collectors.toList());
    }
}
