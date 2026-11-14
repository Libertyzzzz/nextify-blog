package com.nextify.blog.controller.admin;

import com.nextify.blog.common.Result;
import com.nextify.blog.common.annotaion.RequirePermission;
import com.nextify.blog.service.BlogCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/comment")
public class AdminBlogCommentController {

    @Autowired
    private BlogCommentService commentService;

    /**
     * 审核评论
     */
    @RequirePermission("dashboard:comment:audit")
    @PutMapping("/review")
    public Result<Boolean> reviewComment(@RequestParam Long commentId, @RequestParam Integer status){
        return Result.success(commentService.updateStatus(commentId, status));
    }

    /**
     * 删除评论
     */
    @RequirePermission("dashboard:comment:delete")
    @DeleteMapping("/delete")
    public Result<Boolean> deleteComment(@RequestParam Long commentId){
        return Result.success(commentService.deleteCommentById(commentId));
    }
}
