package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("anonymous_user")
public class AnonymousUser {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String anonymousId;
    
    private String fingerprint;
    
    private String ipAddress;
    
    private String browser;
    
    private String os;
    
    private Integer visitCount;

    private String rawFingerprint;

    private Boolean hasCommented;
    
    private LocalDateTime firstVisitTime;
    
    private LocalDateTime lastVisitTime;
}