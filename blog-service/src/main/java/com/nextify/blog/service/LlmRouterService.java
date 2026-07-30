package com.nextify.blog.service;

public interface LlmRouterService {
    String resolveModel(String action);
    String resolveProvider(String model);
}
