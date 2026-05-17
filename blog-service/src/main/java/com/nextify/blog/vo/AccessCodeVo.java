package com.nextify.blog.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class AccessCodeVo {

    private Integer id;
    private String accessCode;
    private String url;
    private String  desc;
    private String extras;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}
