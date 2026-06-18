package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.BlogComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评论记录表 Mapper 接口
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {
}