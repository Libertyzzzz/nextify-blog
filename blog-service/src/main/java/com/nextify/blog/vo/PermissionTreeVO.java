package com.nextify.blog.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限树 VO *
 *  - 与 SysPermission 解耦：实体保持纯粹数据库映射，VO 只暴露前端需要的字段
 *  - children 字段只存在于 VO 中，实体不承载视图结构
 *  - 使用 @JsonInclude(NON_EMPTY) 避免空 children 序列化
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PermissionTreeVO implements Serializable {

    private Long id;

    /** 父级ID (0=顶级) */
    private Long parentId;

    /** 权限编码 */
    private String permCode;

    /** 权限名称 */
    private String permName;

    /** 权限类型 1=菜单 2=按钮 3=API接口 */
    private Integer permType;

    /** 路由路径 (菜单用) */
    private String path;

    /** 前端组件路径 (菜单用) */
    private String component;

    /** 菜单图标 (菜单用) */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 是否可见 1=显示 0=隐藏 */
    private Integer visible;

    /** 状态 1=启用 0=禁用 */
    private Integer status;

    /**
     * 子权限列表
     * 非空时才会序列化到 JSON
     */
    private List<PermissionTreeVO> children;
}