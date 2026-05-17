package com.nextify.blog.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccessCodeAddRequest {

    @NotNull
    private Integer id;

    @NotEmpty
    private String accessCode;

    @NotEmpty
    private String url;


    private String  desc;


    private String extras;
}
