package com.nextify.blog.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserDto {

    @Size(max = 32, message = "昵称长度不能超过32")
    private String nickname;

    private Integer gender;

    private String email;

    private String phone;

    private Integer status;

    private String avatar;

    private String motto;

    @Size(min = 6, max = 32, message = "密码长度需在6-32之间")
    private String password;

    private List<Long> roleIds;
}