package com.nextify.blog.controller;

import com.nextify.blog.common.Result;
import com.nextify.blog.entity.BlogComment;
import com.nextify.blog.service.BlogCommentService;
import com.nextify.blog.utils.IPUtils;
import com.nextify.blog.vo.CommentVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comment")
public class BlogCommentController {
    private final Long GUEST_BOOK_ID = 0L;
    @Autowired
    private BlogCommentService commentService;

    /**
     * 发表评论
     */
    @PostMapping("/publish")
    public Result<Void> post(@RequestBody BlogComment comment, HttpServletRequest request) {

        comment.setIpAddress(IPUtils.getRealIp(request));
        comment.setUserAgent(request.getHeader("User-Agent"));
        commentService.postComment(comment);
        return Result.success();
    }

    /**
     * 获取文章评论树
     */
    @GetMapping("/list/{articleId}")
    public Result<List<CommentVO>> list(@PathVariable Long articleId, @RequestParam(required = false) Integer status) {
        return Result.success(commentService.listByArticleId(articleId, status));
    }

    /**
     * 获取留言树
     * 留言板的文章ID指定为0
     */
    @GetMapping("/list/guest-book")
    public Result<List<CommentVO>> listGuestBook(@RequestParam(required = false) Integer status) {
        return Result.success(commentService.listByArticleId(GUEST_BOOK_ID, status));
    }

    /**
     * 审核评论
     * @return
     */
    @PutMapping("/review")
    public Result<Boolean> reviewComment(@RequestParam Long commentId, @RequestParam Integer status){
        return Result.success(commentService.updateStatus(commentId, status));
    }

    @DeleteMapping("/delete")
    public Result<Boolean> deleteComment(@RequestParam Long commentId){
        return Result.success(commentService.deleteCommentById(commentId));
    }

}