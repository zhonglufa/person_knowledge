-- ============================================================
-- 数据库变更脚本: announcement 表增强
-- 版本: V2.1
-- 日期: 2026-05-04
-- 描述: 1. 修改 status 默认值为 0（草稿）
--       2. 扩展 status 含义支持 -1=已删除
--       3. 增加 type 和复合索引优化筛选查询
-- ============================================================

-- 1. 修改 status 默认值从 1(已发布) 改为 0(草稿)
--    状态定义: -1=已删除, 0=草稿, 1=已发布, 2=已下架, 3=已过期
ALTER TABLE announcement
    MODIFY COLUMN status tinyint NOT NULL DEFAULT 0
        COMMENT '状态: -1=已删除, 0=草稿, 1=已发布, 2=已下架, 3=已过期';

-- 2. 增加 type 字段索引（如已存在则跳过）
--    type 字段已存在: varchar(32) DEFAULT 'system'
--    用于公告类型筛选: system=系统公告, activity=活动通知, maintenance=维护通知
ALTER TABLE announcement
    ADD INDEX idx_type (type);

-- 3. 增加 status+type 复合索引，优化按状态和类型联合筛选
ALTER TABLE announcement
    ADD INDEX idx_status_type (status, type);

-- ============================================================
-- 回滚脚本 (如需回退)
-- ============================================================
-- ALTER TABLE announcement DROP INDEX idx_status_type;
-- ALTER TABLE announcement DROP INDEX idx_type;
-- ALTER TABLE announcement MODIFY COLUMN status tinyint NOT NULL DEFAULT 1
--     COMMENT '状态:1=生效,0=下架';
