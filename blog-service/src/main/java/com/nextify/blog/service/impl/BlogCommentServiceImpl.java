package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.BlogComment;
import com.nextify.blog.enums.CommentStatusEnum;
import com.nextify.blog.mapper.BlogCommentMapper;
import com.nextify.blog.service.AnonymousUserService;
import com.nextify.blog.service.BlogCommentService;
import com.nextify.blog.utils.SensitiveWordFilter;
import com.nextify.blog.vo.CommentVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlogCommentServiceImpl extends ServiceImpl<BlogCommentMapper, BlogComment> implements BlogCommentService {

    @Resource
    private SensitiveWordFilter sensitiveWordFilter;

    @Resource
    private AnonymousUserService anonymousUserService;
    @Override
    @Transactional
    public void postComment(BlogComment comment) {
        // 1. 处理头像逻辑
        if (!StringUtils.hasText(comment.getAvatarUrl())) {
            comment.setAvatarUrl(generateAvatar(comment.getEmail(), comment.getAnonymousId()));
        }
        
        // 2. 默认状态为待审核 (根据需求可设为1直接发布)
        comment.setStatus(CommentStatusEnum.PUBLISHED.getCode());
        String originalContent = comment.getContent();
        if(sensitiveWordFilter.containsSensitiveWord(comment.getContent())){
            log.warn("comments contains sensitive words: {}", originalContent);
            comment.setStatus(CommentStatusEnum.PENDING_REVIEW.getCode());
            comment.setContent(sensitiveWordFilter.replaceSensitiveWord(comment.getContent()));
        }
        // 如果是匿名用户 更新状态
        if(StringUtils.hasText(comment.getAnonymousId())){
            anonymousUserService.updateStatus(comment.getAnonymousId());
        }
        this.save(comment);
    }

    @Override
    public List<CommentVO> listByArticleId(Long articleId, Integer status) {
        // 1. 查询该文章下所有审核通过的评论
        List<BlogComment> comments = this.list(new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getArticleId, articleId)
                .eq(BlogComment::getStatus, status == null ?  CommentStatusEnum.PUBLISHED.getCode() : status)
                .orderByDesc(BlogComment::getCreateTime));

        if (comments.isEmpty()) return new ArrayList<>();

        // 2. 转换为 VO 并建立 ID 映射
        List<CommentVO> voList = comments.stream().map(c -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList());

        Map<Long, CommentVO> voMap = voList.stream().collect(Collectors.toMap(CommentVO::getId, v -> v));

        // 3. 构建树形结构
        List<CommentVO> rootComments = new ArrayList<>();
        for (CommentVO vo : voList) {
            if (vo.getParentId() == null || vo.getParentId() == 0) {
                rootComments.add(vo);
            } else {
                CommentVO parent = voMap.get(vo.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    vo.setParentNickname(parent.getNickname());
                    parent.getChildren().add(vo);
                }
            }
        }
        return rootComments;
    }

    @Override
    public Boolean updateStatus(Long commentId, Integer code) {

        return this.update(new LambdaUpdateWrapper<BlogComment>()
            .eq(BlogComment::getId, commentId)
            .set(BlogComment::getStatus, code)
        );
    }

    @Override
    public Boolean deleteCommentById(Long commentId) {
        return this.removeById(commentId);
    }

    /**
     * 生成头像：优先 Gravatar，其次 DiceBear 随机生成
     */
    private String generateAvatar(String email, String anonymousId) {
        if (StringUtils.hasText(email)) {
            String hash = DigestUtils.md5DigestAsHex(email.trim().toLowerCase().getBytes());
            return "https://www.gravatar.com/avatar/" + hash + "?d=identicon";
        }
        // 使用我们之前的 anonymousId 作为种子生成随机头像
        return "https://api.dicebear.com/7.x/identicon/svg?seed=" + (StringUtils.hasText(anonymousId) ? anonymousId : "guest");
    }
}