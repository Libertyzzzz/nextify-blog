package com.nextify.blog.common.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

@Component
@Slf4j
public class FileValidatorFactory {
    private final Map<String, FileValidator> validatorMap;


    // Spring 自动注入所有校验器
    public FileValidatorFactory(List<FileValidator> validators) {
        this.validatorMap = validators.stream()
            .collect(toMap(FileValidator::getSupportedType, v -> v));
    }

    public FileValidator getInstance(String fileType) {
        if (fileType == null || fileType.trim().isEmpty()) {
            log.warn("文件类型为空，使用默认校验器");
            return validatorMap.get("default");
        }

        return validatorMap.getOrDefault(fileType, validatorMap.get("default"));
    }
}