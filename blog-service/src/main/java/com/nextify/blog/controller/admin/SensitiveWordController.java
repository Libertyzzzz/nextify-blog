package com.nextify.blog.controller.admin;

import cn.hutool.dfa.SensitiveUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.common.Result;
import com.nextify.blog.dto.SensitiveWordAddDto;
import com.nextify.blog.dto.SensitiveWordQueryDto;
import com.nextify.blog.entity.SensitiveWord;
import com.nextify.blog.mapper.SensitiveWordMapper;
import com.nextify.blog.service.SensitiveWordService;
import com.nextify.blog.utils.SensitiveWordFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/sensitive-words")
public class SensitiveWordController {

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 分页查询敏感词
     */
    @GetMapping
    public Result<Page<SensitiveWord>> list(SensitiveWordQueryDto request) {

        return Result.success(sensitiveWordService.list(request));
    }


    /**
     * 添加敏感词
     */
    @PostMapping
    public Result<Void> add(@RequestBody SensitiveWordAddDto request) {
       sensitiveWordService.add(request);
        // 触发内存词树更新
        sensitiveWordFilter.refresh();
        return Result.success();
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        // 触发内存词树更新
        sensitiveWordFilter.refresh();
        return Result.success();
    }
}