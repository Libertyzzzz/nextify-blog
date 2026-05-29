package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.service.SysUserService;
import com.nextify.blog.utils.JwtUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Override
    public Boolean saveUser(SysUserSaveDto request) {
        SysUser user = new SysUser();
        user.setId(request.getId());
        if(request.getUsername() != null)
            user.setUsername( request.getUsername());
        if(request.getPassword() != null)
            user.setPassword(JwtUtils.encrypt(request.getPassword()));
        if(request.getNickname() != null)
            user.setNickname(request.getNickname());
        if(request.getEmail() != null)
            user.setEmail(request.getEmail());
        if(request.getMotto() != null)
            user.setMotto(request.getMotto());
        if(request.getAvatar() != null)
            user.setAvatar(request.getAvatar());
        user.setLastLoginTime(request.getLastLoginTime());
        return saveOrUpdate(user);

    }

}

