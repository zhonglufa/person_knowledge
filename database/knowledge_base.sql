/*
SQLyog Ultimate v12.08 (64 bit)
MySQL - 8.0.38 : Database - knowledge_base
*********************************************************************
*/

/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`knowledge_base` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `knowledge_base`;

/*Table structure for table `admin_operation_log` */

DROP TABLE IF EXISTS `admin_operation_log`;

CREATE TABLE `admin_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `admin_id` bigint NOT NULL COMMENT '操作管理员ID',
  `operation_type` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型: take_down/restore/disable_user/enable_user/publish_announcement/create_announcement/update_announcement',
  `target_type` varchar(32) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '目标类型: collection/note/user/announcement',
  `target_id` bigint DEFAULT NULL COMMENT '目标ID',
  `operation_detail` text COLLATE utf8mb4_unicode_ci COMMENT '操作详情JSON',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员操作日志表';

/*Table structure for table `announcement` */

DROP TABLE IF EXISTS `announcement`;

CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `type` varchar(32) DEFAULT 'system' COMMENT '公告类型: system/activity/maintenance',
  `priority` varchar(16) DEFAULT 'medium' COMMENT '优先级: low/medium/high/urgent',
  `effective_at` datetime DEFAULT NULL COMMENT '生效时间',
  `expire_at` datetime DEFAULT NULL COMMENT '过期时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0=草稿, 1=已发布, 2=已下架, 3=已过期',
  `created_by` bigint NOT NULL COMMENT '创建人(管理员ID)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除:0=未删除,1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_by` (`created_by`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统公告表';

/*Table structure for table `announcement_statistics` */

DROP TABLE IF EXISTS `announcement_statistics`;

CREATE TABLE `announcement_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `announcement_id` bigint NOT NULL COMMENT '公告ID',
  `view_count` int DEFAULT '0' COMMENT '查看次数',
  `read_user_count` int DEFAULT '0' COMMENT '已读用户数',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_announcement` (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公告触达统计表';

/*Table structure for table `category` */

DROP TABLE IF EXISTS `category`;

CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID，关联users.id',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称：计算机-数据库/考研-数学等',
  `parent_id` bigint DEFAULT NULL COMMENT '父分类ID，实现层级分类：0=一级分类',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_parent_id` (`parent_id`),
  CONSTRAINT `category_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `category_ibfk_2` FOREIGN KEY (`parent_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=168 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏项分类表（用户独立，支持层级）';

/*Table structure for table `collection` */

DROP TABLE IF EXISTS `collection`;

CREATE TABLE `collection` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏集主键ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收藏集名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '收藏集描述',
  `user_id` bigint NOT NULL COMMENT '所属用户ID，关联users.id',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动同步）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '收藏集封面图片URL',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开（0=私有，1=公开）',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认收藏集:0=否,1=是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `collection_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Table structure for table `collection_item` */

DROP TABLE IF EXISTS `collection_item`;

CREATE TABLE `collection_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏项主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID，关联users.id',
  `collection_id` bigint DEFAULT NULL COMMENT '所属收藏集ID，关联collection.id',
  `source_type` tinyint NOT NULL COMMENT '来源类型：1=网页,2=图片,3=文本,4=视频',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '收藏项标题（自动抓取/手动输入，非空）',
  `core_abstract` text COMMENT '个人核心摘要(渐进加工，可空)',
  `digest_status` varchar(16) DEFAULT 'undigest' COMMENT '消化状态：undigest-未消化/digesting-消化中/digested-已消化/abandoned-放弃',
  `study_progress` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '0%' COMMENT '学习进度：0%/25%/50%/75%/100%/overdue-逾期',
  `remind_at` datetime DEFAULT NULL COMMENT '学习提醒时间',
  `study_goal` varchar(256) DEFAULT NULL COMMENT '学习目标(渐进加工)',
  `category_id` bigint DEFAULT NULL COMMENT '分类ID（后续拓展分类表时关联）',
  `keywords` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个人提炼关键词,英文逗号分隔',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动同步）',
  `visit_count` int DEFAULT '0' COMMENT '观看次数（缓存值）',
  `source` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '未知' COMMENT '来源名称：知乎/B站/知网等',
  `source_url` varchar(255) DEFAULT NULL COMMENT '来源URL（如网页链接）',
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读：0=否/1=是',
  `is_star` tinyint NOT NULL DEFAULT '0' COMMENT '是否星标:0=否,1=是',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  PRIMARY KEY (`id`),
  KEY `idx_user_collection` (`user_id`,`collection_id`),
  KEY `idx_user_update` (`user_id`,`updated_at`),
  KEY `idx_collection_id` (`collection_id`),
  KEY `idx_user_digest` (`user_id`,`digest_status`),
  KEY `idx_user_progress` (`user_id`,`study_progress`),
  CONSTRAINT `collection_item_ibfk_1` FOREIGN KEY (`collection_id`) REFERENCES `collection` (`id`) ON DELETE CASCADE,
  CONSTRAINT `collection_item_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

/*Table structure for table `collection_item_tag` */

DROP TABLE IF EXISTS `collection_item_tag`;

CREATE TABLE `collection_item_tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID，关联users.id（多用户隔离）',
  `collection_item_id` bigint NOT NULL COMMENT '收藏项ID，关联collection_item.id',
  `tag_id` bigint NOT NULL COMMENT '标签ID，关联tag.id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_item_tag` (`collection_item_id`,`tag_id`),
  KEY `idx_user_item` (`user_id`,`collection_item_id`),
  KEY `idx_user_tag` (`user_id`,`tag_id`),
  KEY `item_tag_ibfk_3` (`tag_id`),
  CONSTRAINT `item_tag_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `item_tag_ibfk_2` FOREIGN KEY (`collection_item_id`) REFERENCES `collection_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `item_tag_ibfk_3` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=24 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏项-标签中间表（多对多关联）';

/*Table structure for table `import_record` */

DROP TABLE IF EXISTS `import_record`;

CREATE TABLE `import_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `import_type` varchar(32) NOT NULL COMMENT '导入类型:browser_bookmark',
  `file_name` varchar(255) DEFAULT NULL COMMENT '文件名',
  `total_count` int NOT NULL DEFAULT '0' COMMENT '总数量',
  `success_count` int NOT NULL DEFAULT '0' COMMENT '成功数量',
  `fail_count` int NOT NULL DEFAULT '0' COMMENT '失败数量',
  `status` varchar(32) NOT NULL DEFAULT 'processing' COMMENT '状态:processing/success/failed',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导入记录表';

/*Table structure for table `interaction_collect` */

DROP TABLE IF EXISTS `interaction_collect`;

CREATE TABLE `interaction_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `target_type` varchar(32) NOT NULL COMMENT '目标类型(note/collection)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除:0=未删除,1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`,`deleted`),
  KEY `idx_target` (`target_id`,`target_type`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='互动收藏表';

/*Table structure for table `interaction_comment` */

DROP TABLE IF EXISTS `interaction_comment`;

CREATE TABLE `interaction_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `target_type` varchar(32) NOT NULL COMMENT '目标类型(note/collection_item/collection)',
  `content` text NOT NULL COMMENT '评论内容',
  `parent_id` bigint DEFAULT NULL COMMENT '父评论ID(回复)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除:0=未删除,1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_id`,`target_type`),
  KEY `idx_parent` (`parent_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='互动评论表';

/*Table structure for table `interaction_like` */

DROP TABLE IF EXISTS `interaction_like`;

CREATE TABLE `interaction_like` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `target_id` bigint NOT NULL COMMENT '目标ID',
  `target_type` varchar(32) NOT NULL COMMENT '目标类型(note/collection_item/collection)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` int NOT NULL DEFAULT '0' COMMENT '逻辑删除:0=未删除,1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`,`target_id`,`target_type`,`deleted`),
  KEY `idx_target` (`target_id`,`target_type`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='互动点赞表';

/*Table structure for table `note` */

DROP TABLE IF EXISTS `note`;

CREATE TABLE `note` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '笔记ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID',
  `collection_item_id` bigint DEFAULT NULL COMMENT '关联的收藏项ID，对应collection_item.id',
  `title` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '笔记标题',
  `note_type` enum('conceptual','procedural','factual','metacognitive') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'conceptual' COMMENT '笔记类型（conceptual=概念性，procedudral=程序性，factual=事实性,metacognitive=元认知）',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '笔记内容（支持简单HTML或纯文本）',
  `is_public` tinyint NOT NULL DEFAULT '0' COMMENT '是否公开（0=私有，1=公开）',
  `views` int unsigned NOT NULL DEFAULT '0' COMMENT '浏览量',
  `likes` int unsigned NOT NULL DEFAULT '0' COMMENT '点赞数',
  `hot_score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '热度评分 = views*0.6 + likes*0.4',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `description` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '笔记描述',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '笔记封面',
  `finshed_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '完成时间',
  `status` enum('草稿','完成') COLLATE utf8mb4_general_ci DEFAULT '草稿',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_note_type` (`note_type`),
  KEY `idx_collection_item_id` (`collection_item_id`),
  FULLTEXT KEY `ft_title` (`title`),
  FULLTEXT KEY `ft_content` (`content`),
  CONSTRAINT `fk_note_collection_item` FOREIGN KEY (`collection_item_id`) REFERENCES `collection_item` (`id`) ON DELETE SET NULL,
  CONSTRAINT `note_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='笔记表';

/*Table structure for table `notification` */

DROP TABLE IF EXISTS `notification`;

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知id',
  `user_id` bigint NOT NULL COMMENT '通知用户id',
  `collection_item_id` bigint NOT NULL COMMENT '对那条收藏进行通知',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT '该收藏需要加工' COMMENT '标题',
  `content` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '内容',
  `is_read` tinyint NOT NULL DEFAULT '0' COMMENT '是否阅读',
  `remind_at` datetime NOT NULL COMMENT '计划提醒时间',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `read_at` datetime DEFAULT NULL COMMENT '用户实际阅读时间',
  `deleted` tinyint(1) NOT NULL COMMENT '是否软删除：0=否/1=是',
  `notify_type` tinyint NOT NULL COMMENT '通知类型：1=收藏项学习提醒/2=系统公告/3=笔记提醒/4=学习进度到期提醒',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`,`is_read`),
  KEY `fk_notify_collection_item` (`collection_item_id`),
  CONSTRAINT `fk_notify_collection_item` FOREIGN KEY (`collection_item_id`) REFERENCES `collection_item` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_notify_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Table structure for table `search_history` */

DROP TABLE IF EXISTS `search_history`;

CREATE TABLE `search_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '所属用户ID，关联users.id',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检索关键词',
  `search_count` int NOT NULL DEFAULT '1' COMMENT '该关键词检索次数',
  `last_search_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后检索时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '首次检索时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_keyword` (`user_id`,`keyword`),
  KEY `idx_user_last_search` (`user_id`,`last_search_at`),
  CONSTRAINT `search_history_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户检索历史表';

/*Table structure for table `search_hot` */

DROP TABLE IF EXISTS `search_hot`;

CREATE TABLE `search_hot` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `keyword` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '检索关键词',
  `search_total` bigint NOT NULL DEFAULT '0' COMMENT '全平台检索总次数',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword` (`keyword`),
  KEY `idx_search_total` (`search_total`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='热门检索词统计表';

/*Table structure for table `tag` */

DROP TABLE IF EXISTS `tag`;

CREATE TABLE `tag` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签id',
  `user_id` bigint NOT NULL COMMENT '所属用户id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '标签颜色（十六进制：#FFFFFF）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动同步）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`name`,`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Table structure for table `user_note_collect` */

DROP TABLE IF EXISTS `user_note_collect`;

CREATE TABLE `user_note_collect` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint NOT NULL COMMENT '用户id',
  `note_id` bigint NOT NULL COMMENT '笔记id',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_note` (`user_id`,`note_id`),
  KEY `fk_unc_note` (`note_id`),
  CONSTRAINT `fk_unc_note` FOREIGN KEY (`note_id`) REFERENCES `note` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_unc_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*Table structure for table `user_settings` */

DROP TABLE IF EXISTS `user_settings`;

CREATE TABLE `user_settings` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `theme` varchar(32) DEFAULT 'light' COMMENT '主题:light/dark',
  `notify_preferences` json DEFAULT NULL COMMENT '通知偏好设置',
  `display_mode` varchar(32) DEFAULT 'grid' COMMENT '显示模式:grid/list',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户个性化设置表';

/*Table structure for table `users` */

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像地址',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间（自动同步）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否软删除：0=否/1=是',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'commonUser' COMMENT '用户角色:commonUser-普通用户/admin-管理员',
  `collection_count` int NOT NULL DEFAULT '0' COMMENT '用户创建的收藏集总数',
  `collection_item_count` int NOT NULL DEFAULT '0' COMMENT '用户收藏的内容总数',
  `note_count` int NOT NULL DEFAULT '0' COMMENT '用户创作的笔记总数',
  `unread_notify_count` int NOT NULL DEFAULT '0' COMMENT '用户未读通知数',
  `last_login_at` datetime DEFAULT NULL COMMENT '用户最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
