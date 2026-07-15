package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.SensitiveWordAddDto;
import com.nextify.blog.dto.SensitiveWordQueryDto;
import com.nextify.blog.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordService {

    Page<SensitiveWord> list(SensitiveWordQueryDto request);

    Boolean add(SensitiveWordAddDto request);

    Boolean delete(Long id);

    void insertBatch(List<SensitiveWord> sensitiveWords);

}
