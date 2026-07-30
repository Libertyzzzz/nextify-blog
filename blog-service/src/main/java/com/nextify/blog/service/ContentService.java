package com.nextify.blog.service;

import java.util.Map;

public interface ContentService {
    Map<String, Object> getArticleContext(String articleId);
}
