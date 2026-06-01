package com.nextify.blog.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FaceDetectVO {
    /** 颜值评分 0-100 */
    private Double beauty;
    /** 年龄 */
    private Integer age;
    /** 性别: male/female */
    private String gender;
}