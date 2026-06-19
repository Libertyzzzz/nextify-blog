package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.entity.BlogCategory;
import com.nextify.blog.dto.BlogCategoryAddRequest;
import com.nextify.blog.dto.BlogCategoryUpdateRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

public interface BlogCategoryService extends IService<BlogCategory> {

    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    List<BlogCategory> getAllCategories();

    /**
     * 分页获取分类列表
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 分页结果
     */
    Page<BlogCategory> getCategoriesByPage(long pageNum, long pageSize);

    /**
     * 新增分类
     * @param request 新增分类请求
     * @return 新增分类的ID
     */
    Long addCategory(BlogCategoryAddRequest request);

    /**
     * 更新分类
     * @param id 分类ID
     * @param request 更新分类请求
     */
    void updateCategory(Long id, BlogCategoryUpdateRequest request);

    /**
     * 删除分类
     * @param id 分类ID
     */
    void deleteCategory(Long id);
}
