package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.common.exception.BusinessException;
import com.nextify.blog.entity.BlogCategory;
import com.nextify.blog.mapper.BlogCategoryMapper;
import com.nextify.blog.service.BlogCategoryService;
import com.nextify.blog.dto.BlogCategoryAddRequest;
import com.nextify.blog.dto.BlogCategoryUpdateRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogCategoryServiceImpl extends ServiceImpl<BlogCategoryMapper, BlogCategory> implements BlogCategoryService {

    @Override
    public List<BlogCategory> getAllCategories() {
        return this.list(new LambdaQueryWrapper<BlogCategory>().orderByAsc(BlogCategory::getSort));
    }

    @Override
    public Page<BlogCategory> getCategoriesByPage(long pageNum, long pageSize) {
        Page<BlogCategory> page = new Page<>(pageNum, pageSize);
        return this.page(page, new LambdaQueryWrapper<BlogCategory>().orderByAsc(BlogCategory::getSort));
    }

    @Override
    public Long addCategory(BlogCategoryAddRequest request) {
        BlogCategory category = new BlogCategory();
        BeanUtils.copyProperties(request, category);
        this.save(category);
        return category.getId();
    }

    @Override
    public void updateCategory(Long id, BlogCategoryUpdateRequest request) {
        BlogCategory existingCategory = this.getById(id);
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }
        BlogCategory category = new BlogCategory();
        BeanUtils.copyProperties(request, category);
        category.setId(id);
        this.updateById(category);
    }

    @Override
    public void deleteCategory(Long id) {
        BlogCategory existingCategory = this.getById(id);
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }
        // TODO: 考虑分类下是否有文章，如果有，需要处理文章的分类归属问题
        this.removeById(id);
    }
}
