package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("online_status")
public class OnlineStatus {

    @TableId(value = "id")
    private String userId;

    @TableField(value =  "is_online")
    private int isOnline;

    @TableField(value = "last_active_time")
    private String lastActiveTime;

    @TableField(value = "session_id")
    private String sessionId;

    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private String createTime;

    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private String updateTime;
}
