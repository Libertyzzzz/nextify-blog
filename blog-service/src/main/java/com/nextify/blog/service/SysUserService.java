package com.nextify.blog.service;

import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;

public interface SysUserService {
    Boolean saveUser(SysUserSaveDto request);
}
