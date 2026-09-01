package com.nextify.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class AddUserDto {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 32, message = "用户名长度需在3-32之间")
    private String username;

    @NotBlank(message = "初始密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String password;

    private String nickname;

    private Integer gender;

    private String email;

    private String phone;

    private Integer status = 1;

    private List<Long> roleIds;
}