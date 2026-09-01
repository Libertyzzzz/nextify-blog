package com.nextify.blog.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysUserVO {
    private String username;

    private String userId;

    /** 博主昵称 */
    private String nickname;


    /** 联系邮箱 */
    private String email;

    private String phone;

    private Integer gender;

    private LocalDateTime createTime;


}
