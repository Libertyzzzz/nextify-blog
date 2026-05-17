package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName(value = "page_access_code")
public class AccessCode {

    @TableId(value = "id")
    private Integer id;

    @TableField(value = "access_code")
    private String accessCode;

    @TableField(value = "url")
    private String url;

    @TableField(value = "url_description")
    private String  urlDescription;

    @TableField(value = "extras")
    private String extras;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;



}
