package com.nextify.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nextify.blog.entity.BlogImage;
import com.nextify.blog.mapper.BlogImageMapper;
import com.nextify.blog.service.ImageService;
import org.springframework.stereotype.Service;

@Service
public class ImageServiceImpl extends ServiceImpl<BlogImageMapper, BlogImage> implements ImageService {
}
