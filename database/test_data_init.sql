-- ============================================
-- 个人结构化知识系统 - 测试数据初始化脚本
-- 用途：为产品首页推荐功能提供展示数据
-- 执行前请确认数据库已创建
-- ============================================

USE `knowledge_base`;

-- 1. 插入测试用户（3个用户）从ID=9开始
INSERT INTO `users` (`id`, `username`, `password`, `email`, `nickname`, `role`, `collection_count`, `collection_item_count`, `note_count`) VALUES
(9, 'testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user1@test.com', '知识达人', 'commonUser', 3, 15, 8),
(10, 'testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user2@test.com', '学习小能手', 'commonUser', 2, 10, 5),
(11, 'testuser3', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'user3@test.com', '编程大师', 'commonUser', 4, 20, 12)
ON DUPLICATE KEY UPDATE username=username;

-- 2. 插入测试收藏集（5个公开收藏集）从ID=13开始
INSERT INTO `collection` (`id`, `name`, `description`, `user_id`, `is_public`, `cover_image`) VALUES
(13, 'Java核心技术笔记', '深入理解Java核心概念，包括集合、并发、JVM等', 9, 1, 'https://picsum.photos/400/300?random=1'),
(14, '前端开发实战指南', 'Vue/React/Angular三大框架实战经验分享', 10, 1, 'https://picsum.photos/400/300?random=2'),
(15, '数据库性能优化', 'MySQL索引优化、查询优化、分库分表实战', 9, 1, 'https://picsum.photos/400/300?random=3'),
(16, '考研数学复习笔记', '高等数学、线性代数、概率论核心考点总结', 11, 1, 'https://picsum.photos/400/300?random=4'),
(17, 'Python数据分析入门', 'Pandas、NumPy、Matplotlib实战教程', 10, 1, 'https://picsum.photos/400/300?random=5')
ON DUPLICATE KEY UPDATE name=name;

-- 3. 插入测试笔记（10个公开+已发布+已消化的笔记）从ID=18开始
INSERT INTO `note` (`id`, `user_id`, `title`, `note_type`, `content`, `description`, `is_public`, `status`, `views`, `likes`, `hot_score`, `cover_image`) VALUES
(18, 9, 'HashMap底层原理解析', 'conceptual', 'HashMap是Java中最常用的数据结构之一，基于数组+链表/红黑树实现...', '深入解析HashMap的扩容机制、哈希冲突解决方案', 1, '完成', 1520, 89, 947.60, 'https://picsum.photos/400/300?random=10'),
(19, 9, 'ConcurrentHashMap并发安全机制', 'conceptual', 'ConcurrentHashMap通过CAS+synchronized实现线程安全...', '对比JDK1.7和JDK1.8的实现差异', 1, '完成', 1230, 67, 764.80, 'https://picsum.photos/400/300?random=11'),
(20, 10, 'Vue3响应式原理详解', 'conceptual', 'Vue3使用Proxy替代Object.defineProperty实现响应式...', '手写迷你版Vue3响应式系统', 1, '完成', 2100, 145, 1318.00, 'https://picsum.photos/400/300?random=12'),
(21, 10, 'React Hooks最佳实践', 'procedural', 'useState、useEffect、useContext等常用Hook的使用场景...', '避免常见陷阱，提升组件性能', 1, '完成', 1850, 112, 1154.80, 'https://picsum.photos/400/300?random=13'),
(22, 11, 'MySQL索引优化实战', 'factual', 'B+树索引结构、覆盖索引、最左前缀原则...', '慢查询优化案例分析', 1, '完成', 2560, 178, 1607.20, 'https://picsum.photos/400/300?random=14'),
(23, 11, 'Linux常用命令速查', 'factual', '文件操作、权限管理、进程管理、网络命令...', '开发者必备的Linux命令清单', 1, '完成', 3200, 201, 2000.40, 'https://picsum.photos/400/300?random=15'),
(24, 9, 'JVM内存模型与垃圾回收', 'conceptual', '堆、栈、方法区的内存划分，GC算法详解...', '通过JVisualVM分析内存泄漏', 1, '完成', 1680, 95, 1046.00, 'https://picsum.photos/400/300?random=16'),
(25, 10, 'TypeScript高级类型技巧', 'procedural', '泛型、条件类型、映射类型、类型守卫...', '提升代码类型安全性', 1, '完成', 1420, 88, 887.20, 'https://picsum.photos/400/300?random=17'),
(26, 11, 'Redis缓存穿透解决方案', 'procedural', '布隆过滤器、缓存空对象、互斥锁...', '高并发场景下的缓存设计', 1, '完成', 1950, 134, 1223.60, 'https://picsum.photos/400/300?random=18'),
(27, 9, 'Spring Boot自动配置原理', 'metacognitive', '@EnableAutoConfiguration、spring.factories、条件注解...', '理解Spring Boot的设计哲学', 1, '完成', 1340, 76, 834.40, 'https://picsum.photos/400/300?random=19')
ON DUPLICATE KEY UPDATE title=title;

-- 4. 插入收藏集项目（可选，用于收藏集详情展示）从ID=29开始
INSERT INTO `collection_item` (`id`, `user_id`, `collection_id`, `source_type`, `title`, `core_abstract`, `source`, `source_url`, `study_progress`, `digest_status`) VALUES
-- Java核心技术笔记 的收藏项
(29, 9, 13, 1, 'Java集合框架详解', 'List/Set/Map的使用场景和底层实现', '博客园', 'https://example.com/java-collections', '100%', 'digested'),
(30, 9, 13, 1, 'Java并发编程实战', '线程池、锁机制、并发容器', '掘金', 'https://example.com/java-concurrent', '75%', 'digesting'),
(31, 9, 13, 1, 'JVM性能调优指南', 'GC调优、内存分析工具使用', 'InfoQ', 'https://example.com/jvm-tuning', '50%', 'digesting'),

-- 前端开发实战指南 的收藏项
(32, 10, 14, 1, 'Vue3 Composition API', 'setup、ref、reactive使用技巧', 'Vue官方文档', 'https://vuejs.org', '100%', 'digested'),
(33, 10, 14, 1, 'React Fiber架构解析', '虚拟DOM diff算法优化', 'React官方博客', 'https://react.dev', '100%', 'digested'),
(34, 10, 14, 1, 'Webpack5模块联邦', '微前端架构实践', 'Webpack文档', 'https://webpack.js.org', '25%', 'undigest'),

-- 数据库性能优化 的收藏项
(35, 9, 15, 1, 'MySQL执行计划分析', 'EXPLAIN命令详解与优化建议', 'MySQL官方文档', 'https://dev.mysql.com', '100%', 'digested'),
(36, 9, 15, 1, 'Redis持久化机制对比', 'RDB vs AOF的选择策略', 'Redis文档', 'https://redis.io', '75%', 'digesting'),
(37, 9, 15, 1, '分库分表实战方案', 'ShardingSphere使用指南', 'Apache文档', 'https://shardingsphere.apache.org', '50%', 'digesting'),

-- 考研数学复习笔记 的收藏项
(38, 11, 16, 1, '高等数学极限与连续', '核心公式与典型例题', 'B站视频', 'https://bilibili.com', '100%', 'digested'),
(39, 11, 16, 1, '线性代数矩阵变换', '特征值与特征向量求解', '知乎专栏', 'https://zhihu.com', '75%', 'digesting'),

-- Python数据分析入门 的收藏项
(40, 10, 17, 1, 'Pandas数据清洗技巧', '缺失值处理、数据转换', 'Pandas文档', 'https://pandas.pydata.org', '100%', 'digested'),
(41, 10, 17, 1, 'Matplotlib可视化实战', '常用图表类型与定制', 'Matplotlib文档', 'https://matplotlib.org', '50%', 'digesting')
ON DUPLICATE KEY UPDATE title=title;

-- 5. 更新用户统计数据
UPDATE `users` SET 
  `collection_count` = (SELECT COUNT(*) FROM `collection` WHERE `user_id` = `users`.`id` AND `deleted` = 0),
  `note_count` = (SELECT COUNT(*) FROM `note` WHERE `user_id` = `users`.`id` AND `deleted` = 0),
  `collection_item_count` = (SELECT COUNT(*) FROM `collection_item` WHERE `user_id` = `users`.`id` AND `deleted` = 0)
WHERE `id` IN (9, 10, 11);

-- ============================================
-- 数据验证查询
-- ============================================

-- 查看用户数据
SELECT id, username, nickname, role FROM users WHERE id IN (9,10,11);

-- 查看公开收藏集
SELECT id, name, user_id, is_public FROM collection WHERE is_public = 1 AND deleted = 0;

-- 查看推荐笔记（公开+完成状态）
SELECT id, title, user_id, is_public, status, views, likes, hot_score 
FROM note 
WHERE is_public = 1 AND status = '完成' AND deleted = 0 
ORDER BY hot_score DESC 
LIMIT 10;

-- 查看收藏集项目
SELECT ci.id, ci.title, c.name as collection_name, ci.study_progress, ci.digest_status
FROM collection_item ci
LEFT JOIN collection c ON ci.collection_id = c.id
WHERE ci.deleted = 0
ORDER BY ci.collection_id, ci.id;

-- ============================================
-- 执行完成后，刷新产品首页查看效果
-- 访问: http://localhost:5173/guest/home
-- ============================================
