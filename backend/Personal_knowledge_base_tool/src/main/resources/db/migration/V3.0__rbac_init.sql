-- ============================================================
-- 数据库变更脚本: RBAC 权限系统初始化
-- 版本: V3.0
-- 日期: 2026-05-04
-- 描述: 1. 创建 RBAC 相关表结构
--       2. 初始化角色数据（common_user, admin, super_admin）
--       3. 初始化权限数据（14 个权限码）
--       4. 建立角色-权限关联关系
--       5. 创建索引优化查询性能
-- 特性: 幂等执行（重复执行不报错）
-- ============================================================

-- ============================================================
-- 1. 创建角色表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_code`   VARCHAR(50)  NOT NULL COMMENT '角色编码，如 common_user/admin/super_admin',
    `role_name`   VARCHAR(100) NOT NULL COMMENT '角色名称',
    `role_desc`   VARCHAR(255) DEFAULT NULL COMMENT '角色描述',
    `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================
-- 2. 创建权限表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_permission` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `permission_code` VARCHAR(100) NOT NULL COMMENT '权限编码，如 user:view / announcement:create',
    `permission_name` VARCHAR(100) NOT NULL COMMENT '权限名称',
    `permission_type` VARCHAR(20)  NOT NULL COMMENT '权限类型：menu/page/button/api',
    `module`          VARCHAR(50)  DEFAULT NULL COMMENT '所属模块：user/announcement/content/taxonomy/admin',
    `path`            VARCHAR(255) DEFAULT NULL COMMENT '前端路由或后端接口路径',
    `method`          VARCHAR(20)  DEFAULT NULL COMMENT '接口请求方法：GET/POST/PUT/DELETE',
    `sort_no`         INT          NOT NULL DEFAULT 0 COMMENT '排序号',
    `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`),
    KEY `idx_module` (`module`),
    KEY `idx_type` (`permission_type`),
    KEY `idx_status` (`status`),
    KEY `idx_status_deleted_module` (`status`, `deleted`, `module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- ============================================================
-- 3. 创建用户-角色关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `id`         BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT   NOT NULL COMMENT '用户ID，关联 users.id',
    `role_id`    BIGINT   NOT NULL COMMENT '角色ID，关联 sys_role.id',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_sys_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户-角色关联表';

-- ============================================================
-- 4. 创建角色-权限关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS `sys_role_permission` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id`       BIGINT   NOT NULL COMMENT '角色ID，关联 sys_role.id',
    `permission_id` BIGINT   NOT NULL COMMENT '权限ID，关联 sys_permission.id',
    `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission` (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_sys_role_permission_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_sys_role_permission_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色-权限关联表';

-- ============================================================
-- 5. 初始化角色数据（幂等：存在则跳过）
-- ============================================================
INSERT IGNORE INTO `sys_role` (`id`, `role_code`, `role_name`, `role_desc`, `status`) VALUES
(1, 'common_user', '普通用户', '前台普通注册用户', 1),
(2, 'admin', '管理员', '后台管理员', 1),
(3, 'super_admin', '超级管理员', '拥有全部后台权限', 1);

-- ============================================================
-- 6. 初始化权限数据（幂等：存在则跳过）
-- ============================================================
INSERT IGNORE INTO `sys_permission` (`id`, `permission_code`, `permission_name`, `permission_type`, `module`, `path`, `method`, `sort_no`, `status`) VALUES
-- admin 模块
(1,  'admin:dashboard:view', '查看后台首页', 'page',   'admin',        '/admin/home',                          'GET',    10, 1),
-- user 模块
(2,  'user:view',            '查看用户列表', 'page',   'user',         '/admin/users',                         'GET',    20, 1),
(3,  'user:create',          '创建用户',     'button', 'user',         '/admin/users',                         'POST',   21, 1),
(4,  'user:update',          '编辑用户',     'button', 'user',         '/admin/users/:id',                     'PUT',    22, 1),
(5,  'user:disable',         '禁用用户',     'button', 'user',         '/admin/users/:id/disable',             'POST',   23, 1),
-- content 模块
(6,  'content:view',         '查看内容管理', 'page',   'content',      '/admin/content',                       'GET',    30, 1),
(7,  'content:take_down',    '下架内容',     'button', 'content',      '/admin/content/:id/take-down',         'POST',   31, 1),
(8,  'content:restore',      '恢复内容',     'button', 'content',      '/admin/content/:id/restore',           'POST',   32, 1),
-- announcement 模块
(9,  'announcement:view',    '查看公告管理', 'page',   'announcement', '/admin/announcements',                 'GET',    40, 1),
(10, 'announcement:create',  '创建公告',     'button', 'announcement', '/admin/announcements',                 'POST',   41, 1),
(11, 'announcement:update',  '编辑公告',     'button', 'announcement', '/admin/announcements/:id',             'PUT',    42, 1),
(12, 'announcement:publish', '发布公告',     'button', 'announcement', '/admin/announcements/:id/publish',     'POST',   43, 1),
-- taxonomy 模块
(13, 'taxonomy:view',        '查看分类标签管理', 'page',   'taxonomy', '/taxonomy',                            'GET',    50, 1),
(14, 'taxonomy:manage',      '管理分类标签',     'button', 'taxonomy', '/taxonomy',                            'POST',   51, 1);

-- ============================================================
-- 7. 初始化角色-权限关联数据（幂等：存在则跳过）
-- ============================================================

-- common_user: 仅前台查看权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 6),   -- content:view
(1, 9),   -- announcement:view
(1, 13);  -- taxonomy:view

-- admin: 拥有全部权限
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2, 1),   -- admin:dashboard:view
(2, 2),   -- user:view
(2, 3),   -- user:create
(2, 4),   -- user:update
(2, 5),   -- user:disable
(2, 6),   -- content:view
(2, 7),   -- content:take_down
(2, 8),   -- content:restore
(2, 9),   -- announcement:view
(2, 10),  -- announcement:create
(2, 11),  -- announcement:update
(2, 12),  -- announcement:publish
(2, 13),  -- taxonomy:view
(2, 14);  -- taxonomy:manage

-- super_admin: 拥有全部权限（与 admin 相同，后续可扩展更高权限）
INSERT IGNORE INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3, 1),   -- admin:dashboard:view
(3, 2),   -- user:view
(3, 3),   -- user:create
(3, 4),   -- user:update
(3, 5),   -- user:disable
(3, 6),   -- content:view
(3, 7),   -- content:take_down
(3, 8),   -- content:restore
(3, 9),   -- announcement:view
(3, 10),  -- announcement:create
(3, 11),  -- announcement:update
(3, 12),  -- announcement:publish
(3, 13),  -- taxonomy:view
(3, 14);  -- taxonomy:manage

-- ============================================================
-- 验证脚本
-- ============================================================
-- SELECT 'roles' AS table_name, COUNT(*) AS cnt FROM sys_role
-- UNION ALL
-- SELECT 'permissions', COUNT(*) FROM sys_permission
-- UNION ALL
-- SELECT 'role_permissions', COUNT(*) FROM sys_role_permission;
