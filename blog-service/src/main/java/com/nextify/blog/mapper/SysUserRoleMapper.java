package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper 接口
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
