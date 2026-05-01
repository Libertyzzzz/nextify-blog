package com.nextify.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 网站全局配置实体类
 * 对应表：blog_config
 */
@Data
@TableName("blog_config")
public class BlogConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 配置项键 (如 site_name, icp_no) */
    @TableField("config_key")
    private String configKey;

    /** 配置项值 */
    @TableField("config_value")
    private String configValue;

    /** 配置描述 */
    private String remark;
}