package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.SensitiveWordAddDto;
import com.nextify.blog.dto.SensitiveWordQueryDto;
import com.nextify.blog.entity.SensitiveWord;
import com.nextify.blog.mapper.SensitiveWordMapper;
import com.nextify.blog.service.SensitiveWordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    @Override
    public Boolean add(SensitiveWordAddDto request) {
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(request.getWord());
        entity.setCategory(request.getCategory());
        return sensitiveWordMapper.insert(entity) > 0;
    }

    @Override
    public Page<SensitiveWord> list(SensitiveWordQueryDto request) {
        Page<SensitiveWord> page = new Page<>(request.getPageNum(), request.getPageSize());
        return sensitiveWordMapper.selectPage(page, null);

    }

    @Override
    public Boolean delete(Long id) {
        return sensitiveWordMapper.deleteById(id) > 0;
    }

    @Override
    public void insertBatch(List<SensitiveWord> sensitiveWords) {
        this.saveBatch(sensitiveWords);
    }
}
