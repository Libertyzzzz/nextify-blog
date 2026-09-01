-- =============================================================================
-- RBAC 初始化数据
-- 创建时间: 2026-08-27
-- =============================================================================

-- ========== 角色 ==========
INSERT INTO sys_role (id, role_code, role_name, description, sort, status, is_system) VALUES
(1, 'ROLE_SUPER_ADMIN', '超级管理员', '拥有全部权限，不可删除', 1, 1, 1),
(2, 'ROLE_ADMIN',       '管理员',     '后台管理权限',           2, 1, 1),
(3, 'ROLE_EDITOR',      '编辑者',     '文章编辑权限',           3, 1, 0),
(4, 'ROLE_USER',        '普通用户',   '基础阅读权限',           4, 1, 1)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ========== 权限 (菜单) ==========
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, icon, sort) VALUES
(1, 0, 'dashboard',       '控制台',   1, '/dashboard',       'Monitor',   0),
(2, 0, 'content',         '内容管理', 1, '/content',         'Document',  1),
(3, 0, 'system',          '系统管理', 1, '/system',          'Setting',   2)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 内容管理子菜单
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, component, sort) VALUES
(20, 2, 'content:article',  '文章管理', 1, '/content/article',  'content/Article',  1),
(21, 2, 'content:category', '分类管理', 1, '/content/category', 'content/Category', 2),
(22, 2, 'content:tag',      '标签管理', 1, '/content/tag',      'content/Tag',      3),
(23, 2, 'content:comment',  '评论管理', 1, '/content/comment',  'content/Comment',  4)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- 系统管理子菜单
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type, path, component, sort) VALUES
(30, 3, 'system:user',      '用户管理', 1, '/system/user',      'system/User',      1),
(31, 3, 'system:role',      '角色管理', 1, '/system/role',      'system/Role',      2),
(32, 3, 'system:permission','权限管理', 1, '/system/permission','system/Permission',3),
(33, 3, 'system:log',       '日志管理', 1, '/system/log',       'system/Log',       4)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- ========== 按钮权限 ==========
INSERT INTO sys_permission (id, parent_id, perm_code, perm_name, perm_type) VALUES
(100, 30, 'system:user:add',       '新增用户', 2),
(101, 30, 'system:user:edit',      '编辑用户', 2),
(102, 30, 'system:user:delete',    '删除用户', 2),
(103, 30, 'system:user:resetPwd',  '重置密码', 2),
(200, 20, 'content:article:add',   '发布文章', 2),
(201, 20, 'content:article:edit',  '编辑文章', 2),
(202, 20, 'content:article:delete','删除文章', 2),
(203, 20, 'content:article:audit', '审核文章', 2)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- ========== 角色-权限关联 ==========
-- 超级管理员：全部权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 管理员：除用户删除外的大部分权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),(2, 2),(2, 3),
(2, 20),(2, 21),(2, 22),(2, 23),
(2, 30),(2, 31),(2, 32),
(2, 100),(2, 101),(2, 103),
(2, 200),(2, 201),(2, 202),(2, 203);

-- 编辑者：内容管理权限
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES
(3, 1),(3, 2),
(3, 20),(3, 21),(3, 22),
(3, 200),(3, 201);

-- 普通用户：仅控制台
INSERT IGNORE INTO sys_role_permission (role_id, permission_id) VALUES (4, 1);

-- ========== 为现有用户分配超级管理员角色 ==========
-- 请根据实际 sys_user 表中的有效 ID 修改
-- INSERT IGNORE INTO sys_user_role (user_id, role_id) VALUES (1, 1);
