package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.AnonymousUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 匿名用户数据访问层
 */
@Mapper
public interface AnonymousUserMapper extends BaseMapper<AnonymousUser> {
}