package com.nextify.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SysUserSaveDto {
    @NotNull(message = "用户ID不能为空")
    private Long id;
    private String username;
    private String password;
    private String nickname;
    private String email;
    private String motto;
    private String avatar;
}
