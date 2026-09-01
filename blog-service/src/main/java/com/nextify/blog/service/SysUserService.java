package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nextify.blog.dto.AddUserDto;
import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.dto.UpdateUserDto;
import com.nextify.blog.entity.SysUser;

public interface SysUserService {

    @Deprecated
    Boolean saveUser(SysUserSaveDto request);

    SysUser findByUserId(Long userId);

    Page<SysUser> listUsersPage(int current, int size, String keyword, Integer status);

    SysUser createUser(AddUserDto dto);

    SysUser updateUser(Long userId, UpdateUserDto dto);
}