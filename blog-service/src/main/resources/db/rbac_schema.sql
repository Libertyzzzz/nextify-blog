-- =============================================================================
-- RBAC 权限系统表结构
-- 创建时间: 2026-08-27
-- =============================================================================

-- 1. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`     VARCHAR(64)  NOT NULL COMMENT '角色编码 (唯一标识，如 ROLE_ADMIN)',
    `role_name`     VARCHAR(64)  NOT NULL COMMENT '角色名称 (显示用)',
    `description`   VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `sort`          INT          NOT NULL DEFAULT 0 COMMENT '排序 (数值越小越靠前)',
    `data_scope`    TINYINT      NOT NULL DEFAULT 1 COMMENT '数据权限范围 1=全部 2=本部门 3=本部门及以下 4=仅本人',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    `is_system`     TINYINT      NOT NULL DEFAULT 0 COMMENT '系统内置角色不可删除',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 2. 权限表 (菜单 + 按钮 + API)
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `parent_id`    BIGINT       NOT NULL DEFAULT 0 COMMENT '父级ID (0=顶级)',
    `perm_code`    VARCHAR(128) NOT NULL COMMENT '权限编码 (全局唯一，如 article:create)',
    `perm_name`    VARCHAR(64)  NOT NULL COMMENT '权限名称 (显示用)',
    `perm_type`    TINYINT      NOT NULL COMMENT '权限类型 1=菜单 2=按钮 3=API接口',
    `path`         VARCHAR(255) DEFAULT NULL COMMENT '路由路径 (菜单用)',
    `component`    VARCHAR(255) DEFAULT NULL COMMENT '前端组件路径 (菜单用)',
    `icon`         VARCHAR(64)  DEFAULT NULL COMMENT '菜单图标 (菜单用)',
    `sort`         INT          NOT NULL DEFAULT 0 COMMENT '排序',
    `visible`      TINYINT      NOT NULL DEFAULT 1 COMMENT '是否可见 1=显示 0=隐藏',
    `status`       TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1=启用 0=禁用',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_perm_code` (`perm_code`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表 (菜单/按钮/API)';

-- 3. 用户角色关联表
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT   NOT NULL COMMENT '用户ID → sys_user.id',
    `role_id`     BIGINT   NOT NULL COMMENT '角色ID → sys_role.id',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`       BIGINT   NOT NULL COMMENT '角色ID → sys_role.id',
    `permission_id` BIGINT   NOT NULL COMMENT '权限ID → sys_permission.id',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 登录日志表 (预留扩展点，本期不实现)
CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       DEFAULT NULL COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '登录账号',
    `login_type`  TINYINT      NOT NULL COMMENT '登录类型 1=密码 2=扫码 3=第三方',
    `status`      TINYINT      NOT NULL COMMENT '登录结果 1=成功 0=失败',
    `ip`          VARCHAR(45)  DEFAULT NULL COMMENT '登录IP',
    `user_agent`  VARCHAR(512) DEFAULT NULL COMMENT 'User-Agent',
    `message`     VARCHAR(255) DEFAULT NULL COMMENT '消息/错误信息',
    `login_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表 (预留扩展)';

-- 6. sys_user 表新增 status 字段
ALTER TABLE `sys_user` ADD COLUMN IF NOT EXISTS `status` TINYINT NOT NULL DEFAULT 1 COMMENT '【RBAC新增】账号状态 1=启用 0=禁用';
