package com.nextify.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nextify.blog.entity.BlogArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {

    /**
     * 增加点击量
     */
    @Update("UPDATE blog_article SET view_count = view_count + 1 WHERE id = #{id}")
    int incrementViewCount(Long id);
}
