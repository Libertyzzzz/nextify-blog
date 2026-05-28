package com.nextify.blog.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.AccessCodeAddRequest;
import com.nextify.blog.entity.AccessCode;
import com.nextify.blog.mapper.AccessCodeMapper;
import com.nextify.blog.service.AccessCodeService;
import com.nextify.blog.utils.JwtUtils;
import com.nextify.blog.vo.AccessCodeVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AccessCodeServiceImpl extends ServiceImpl<AccessCodeMapper, AccessCode>  implements AccessCodeService {

    @Resource
    private  AccessCodeMapper accessCodeMapper;
    @Override
    public Boolean verify(Integer id, String accessCode) {
        LambdaQueryWrapper<AccessCode> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AccessCode::getId, id);
        AccessCode curr = accessCodeMapper.selectOne(wrapper);
        return JwtUtils.verify(accessCode, curr.getAccessCode());
    }

    @Override
    public AccessCodeVo getAccessCodeVo(Integer id) {

        AccessCode curr = accessCodeMapper.selectById(id);
        if(curr == null)
            return null;
        return AccessCodeVo.builder()
            .id(curr.getId())
            .status(curr.getStatus())
            .build();
    }

    @Override
    public Integer save(AccessCodeAddRequest request) {
        AccessCode curr = new AccessCode();
        curr.setId(request.getId());
        curr.setAccessCode(JwtUtils.encrypt(request.getAccessCode()));
        curr.setUrl(request.getUrl());
        curr.setUrlDescription(request.getDesc());
        curr.setExtras(request.getExtras());
        return accessCodeMapper.insert(curr);
    }
}
