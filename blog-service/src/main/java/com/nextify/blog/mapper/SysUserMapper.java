package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper 接口
 * 继承 BaseMapper 即可获得常用的 CRUD 能力
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}