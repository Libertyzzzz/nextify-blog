package com.nextify.blog.service;

import java.util.List;

public interface ImageFileService {
    void deletePhysicalImageAsync(List<String> fileNames);
}
