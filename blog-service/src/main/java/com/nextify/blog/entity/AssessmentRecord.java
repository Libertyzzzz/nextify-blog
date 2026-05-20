package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import jakarta.annotation.Resource;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@TableName(value = "assessment_records")
public class AssessmentRecord {
    private String shareId;
    private String gender;
    private Integer score;
    private String marketLevel;
    private Float lieFactor;

    @TableField(value = "report")
    private String report;

    @TableField(value = "radar_data")
    private String radarData;  // 六维雷达数据

    @TableField(value = "region")
    private String region;

    @TableField(value = "ip")
    private String ip;

    @TableField(value = "isp")
    private String isp;

    @TableField(value = "raw_input")
    private String rawInput; // 原始输入数据

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "expires_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime expiredTime;
}
