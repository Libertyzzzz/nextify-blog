package com.nextify.blog.service.impl;

import com.nextify.blog.service.ImageFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@Slf4j
public class ImageFileServiceImpl implements ImageFileService {
    @Value("${nextify.upload.local-path}")
    private String uploadLocalPath;

    @Override
    @Async("taskExecutor")
    public void deletePhysicalImageAsync(List<String> fileNames) {
        if(CollectionUtils.isEmpty(fileNames))
                return ;
        // 路径规范化：Paths.get 将字符串转为路径对象。toAbsolutePath() 确保它是绝对路径，
        // normalize() 会去掉路径中的 . 或 ..。这是为了防止目录遍历攻击（比如文件名里藏着 ../）
        // 确保操作始终在上传目录下。
        Path rootPath = Paths.get(uploadLocalPath).toAbsolutePath().normalize();
        for(String name : fileNames){
            try {
                //路径解析：resolve(name) 把根目录和文件名拼接在一起。
                // 再次 normalize() 是为了双重保险，确保生成的路径是合法的物理路径。
                Path filePath  = rootPath.resolve(name).normalize();
                if(Files.deleteIfExists(filePath)){
                    log.info("磁盘物理删除成功: {}", filePath);
                }
            }catch (Exception  e){
                log.error("磁盘物理删除失败: {}, 错误原因：{}", name, e.getMessage());
            }
        }

    }
}
