package com.nextify.blog.dto;

import lombok.Data;

@Data
public class PermissionTreeDto {

    /**
     * 起始节点 parentId
     * null / 0L → 从根节点 (parentId=0) 开始返回完整树
     * 具体值   → 只返回该节点下的完整子树
     */
    private Long parentId;

    /**
     * 权限类型过滤 1=菜单 2=按钮 3=API接口
     * null 表示不按类型过滤
     */
    private Integer permType;

    /**
     * 是否只查启用状态的权限
     * 默认 true，前端渲染菜单时只需要启用的
     */
    private Boolean onlyEnabled = true;

}
