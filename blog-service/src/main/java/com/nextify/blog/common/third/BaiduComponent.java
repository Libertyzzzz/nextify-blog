package com.nextify.blog.common.third;

import com.alibaba.fastjson2.JSON;
import com.nextify.blog.vo.BaiduFaceResponse;
import com.nextify.blog.vo.BaiduTokenResponse;
import com.nextify.blog.vo.FaceDetectVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 百度云 AI 颜值分析实现
 */
@Slf4j
@Component
public class BaiduComponent {

    @Value("${nextify.ai-face.baidu-api-key}")
    private String apiKey;

    @Value("${nextify.ai-face.baidu-secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private String accessToken = null;
    private long tokenExpireTime = 0;

    /**
     * 获取 Access Token (百度 Token 有效期 30 天，这里简单做个内存缓存)
     */
    private String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=" + apiKey + "&client_secret=" + secretKey;
        try {
            BaiduTokenResponse response = restTemplate.getForObject(url, BaiduTokenResponse.class);
            if (response != null && response.getAccessToken() != null) {
                this.accessToken = response.getAccessToken();
                // 30天有效期，保险起见设为 25 天后过期
                this.tokenExpireTime = System.currentTimeMillis() + (response.getExpiresIn() - 500000) * 1000;
                return accessToken;
            }
        } catch (Exception e) {
            log.error("获取百度AI Token失败", e);
        }
        return null;
    }

    /**
     * 调用人脸检测接口获取颜值
     */
    public FaceDetectVO detectFace(byte[] imageBytes) {
        String token = getAccessToken();
        if (token == null) return null;

        String url = "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=" + token;

        try {
            String base64Img = Base64.getEncoder().encodeToString(imageBytes);
            Map<String, Object> params = new HashMap<>();
            params.put("image", base64Img);
            params.put("image_type", "BASE64");
            // 必须指定 beauty, age, gender 字段
            params.put("face_field", "beauty,age,gender");

            BaiduFaceResponse response = restTemplate.postForObject(url, params, BaiduFaceResponse.class);
            log.info("百度AI识别人脸原始响应: {}", JSON.toJSONString(response));

            if (response != null && response.getResult() != null) {
                List<BaiduFaceResponse.FaceInfo> faceList = response.getResult().getFaceList();
                if (faceList != null && !faceList.isEmpty()) {
                    BaiduFaceResponse.FaceInfo face = faceList.get(0);
                    return FaceDetectVO.builder()
                            .beauty(face.getBeauty())
                            .age(face.getAge())
                            .gender(face.getGender().getType())
                            .build();
                }
            } else {
                log.warn("百度AI识别人脸失败: {}", JSON.toJSONString(response));
            }
        } catch (Exception e) {
            log.error("调用百度人脸识别接口异常", e);
        }
        return null;
    }
}