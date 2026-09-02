package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.dto.AddUserDto;
import com.nextify.blog.dto.SysUserSaveDto;
import com.nextify.blog.dto.UpdateUserDto;
import com.nextify.blog.entity.SysUser;
import com.nextify.blog.mapper.SysUserMapper;
import com.nextify.blog.service.RoleService;
import com.nextify.blog.service.SysUserService;
import com.nextify.blog.vo.SysUserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleService roleService;

    @Override
    @Deprecated
    public Boolean saveUser(SysUserSaveDto request) {
        SysUser user = new SysUser();
        user.setId(request.getId());
        if(request.getUsername() != null)
            user.setUsername( request.getUsername());
        if(request.getPassword() != null)
            user.setPassword(passwordEncoder.encode(request.getPassword()));
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

    @Override
    public SysUser findByUserId(Long userId) {
        return userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, userId));
    }

    @Override
    public Page<SysUserVO> listUsersPage(int current, int size, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(SysUser::getUsername, keyword)
                .or()
                .like(SysUser::getNickname, keyword)
                .or()
                .like(SysUser::getEmail, keyword)
            );
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> pageResult = userMapper.selectPage(page, wrapper);
        List<SysUser> data = pageResult.getRecords();
        List<SysUserVO> convertData = data.stream()
                .map(item -> {
                    SysUserVO curr = new SysUserVO();
                    BeanUtils.copyProperties(item, curr);
                    curr.setUserId(String.valueOf(item.getUserId()));
                    return curr;
                })
                    .toList();
        Page<SysUserVO> res = new Page<>(current, size, pageResult.getTotal());
        res.setRecords(convertData);
        return res;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser createUser(AddUserDto dto) {
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new IllegalArgumentException("用户名已存在: " + dto.getUsername());
        }

        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setGender(dto.getGender() != null ? dto.getGender() : 0);
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        userMapper.insert(user);
        log.info("新增用户: userId={}, username={}", user.getId(), user.getUsername());

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            roleService.assignRolesToUser(user.getId(), dto.getRoleIds());
        }

        user.setPassword(null);
        return user;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUser(Long userId, UpdateUserDto dto) {
        SysUser existing = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>().
                eq(SysUser::getUserId, userId)
        );
        if (existing == null) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }

        if (dto.getNickname() != null) existing.setNickname(dto.getNickname());
        if (dto.getGender() != null)   existing.setGender(dto.getGender());
        if (dto.getEmail() != null)    existing.setEmail(dto.getEmail());
        if (dto.getPhone() != null)    existing.setPhone(dto.getPhone());
        if (dto.getStatus() != null)   existing.setStatus(dto.getStatus());
        if (dto.getAvatar() != null)   existing.setAvatar(dto.getAvatar());
        if (dto.getMotto() != null)    existing.setMotto(dto.getMotto());

        if (StringUtils.hasText(dto.getPassword())) {
            existing.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userMapper.updateById(existing);

        if (dto.getRoleIds() != null) {
            roleService.assignRolesToUser(userId, dto.getRoleIds());
        }

        existing.setPassword(null);
        return existing;
    }
}