package com.nextify.blog.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * 百度人脸检测响应对象
 */
@Data
public class BaiduFaceResponse {

    private FaceResult result;

    @Data
    public static class FaceResult {
        @JsonProperty("face_list")
        private List<FaceInfo> faceList;
    }

    @Data
    public static class FaceInfo {
        private Double beauty;
        private Integer age;
        private GenderInfo gender;
    }

    @Data
    public static class GenderInfo {
        /**
         * male:男性 female:女性
         */
        private String type;
    }
}