package com.nextify.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nextify.blog.entity.BlogComment;
import com.nextify.blog.vo.CommentVO;
import java.util.List;

public interface BlogCommentService extends IService<BlogComment> {
    void postComment(BlogComment comment);
    List<CommentVO> listByArticleId(Long articleId, Integer status);
    Boolean updateStatus(Long commentId, Integer code);
    Boolean deleteCommentById(Long commentId);
}