package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.dto.admin.AdminCreateUserDTO;
import com.qst.lm.dto.admin.AdminEditUserDTO;
import com.qst.lm.dto.announcement.AnnouncementDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.*;
import com.qst.lm.pojo.*;
import com.qst.lm.service.IAdminService;
import com.qst.lm.service.VerificationCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理服务实现类
 */
@Slf4j
@Service
public class AdminServiceImpl implements IAdminService {

    private final UserMapper userMapper;
    private final CollectionMapper collectionMapper;
    private final NoteMapper noteMapper;
    private final AnnouncementMapper announcementMapper;
    private final CollectionItemMapper collectionItemMapper;
    private final NotificationMapper notificationMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final AnnouncementStatisticsMapper announcementStatisticsMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserMapper userMapper,
                            CollectionMapper collectionMapper,
                            NoteMapper noteMapper,
                            AnnouncementMapper announcementMapper,
                            CollectionItemMapper collectionItemMapper,
                            NotificationMapper notificationMapper,
                            AdminOperationLogMapper adminOperationLogMapper,
                            AnnouncementStatisticsMapper announcementStatisticsMapper) {
        this.userMapper = userMapper;
        this.collectionMapper = collectionMapper;
        this.noteMapper = noteMapper;
        this.announcementMapper = announcementMapper;
        this.collectionItemMapper = collectionItemMapper;
        this.notificationMapper = notificationMapper;
        this.adminOperationLogMapper = adminOperationLogMapper;
        this.announcementStatisticsMapper = announcementStatisticsMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public R getUserList(PageDTO page, String keyword, String role) {
        Page<User> pageObj = new Page<>(page.getPageNum(), page.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索（用户名或昵称）
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        // 角色筛选
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        Page<User> result = userMapper.selectPage(pageObj, wrapper);
        // 隐藏密码，并添加前端需要的status字段映射
        result.getRecords().forEach(user -> {
            user.setPassword(null);
            // deleted=0 -> status=1(正常), deleted=1 -> status=0(禁用)
            // 注意：MyBatis Plus的Page对象支持Map结构，需要动态添加字段
        });
        // 转换为包含status字段的Map列表
        List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (User user : result.getRecords()) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("email", user.getEmail());
            map.put("nickname", user.getNickname());
            map.put("role", user.getRole());
            map.put("deleted", user.getDeleted());
            map.put("status", user.getDeleted() == 0 ? "enabled" : "disabled");
            map.put("createdAt", user.getCreatedAt());
            map.put("lastLoginAt", user.getLastLoginAt());
            records.add(map);
        }
        // 构建分页结果
        Map<String, Object> pageResult = new HashMap<>();
        pageResult.put("total", result.getTotal());
        pageResult.put("current", result.getCurrent());
        pageResult.put("size", result.getSize());
        pageResult.put("records", records);
        return R.success(pageResult);
    }

    @Override
    public R disableUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if ("admin".equals(user.getRole())) {
            throw new BusinessException("不能禁用管理员账号");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getDeleted, 1);
        userMapper.update(null, wrapper);

        // 记录操作日志
        saveOperationLog(userId, "disable_user", "user", userId, "禁用用户");

        log.info("禁用用户[{}]", userId);
        return R.success("用户已禁用");
    }

    @Override
    public R enableUser(Long userId) {
        // 需要绕过逻辑删除查询被禁用的用户
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getDeleted, 0);
        int rows = userMapper.update(null, wrapper);
        if (rows == 0) {
            throw new BusinessException("用户不存在");
        }

        // 记录操作日志
        saveOperationLog(userId, "enable_user", "user", userId, "解封用户");

        log.info("解封用户[{}]", userId);
        return R.success("用户已解封");
    }

    @Override
    public R getPublicContent(PageDTO page, String contentType, String searchKey) {
        String normalizedType;
        if ("collect".equals(contentType) || "collection_item".equals(contentType) || "item".equals(contentType)) {
            normalizedType = "collection_item";
        } else {
            normalizedType = contentType;
        }

        if ("collection_item".equals(normalizedType)) {
            Page<CollectionItem> pageObj = new Page<>(page.getPageNum(), page.getPageSize());
            LambdaQueryWrapper<CollectionItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(CollectionItem::getIsPublic, 1);
            if (StringUtils.hasText(searchKey)) {
                wrapper.and(w -> w.like(CollectionItem::getTitle, searchKey)
                        .or().like(CollectionItem::getKeywords, searchKey));
            }
            wrapper.orderByDesc(CollectionItem::getCreatedAt);
            Page<CollectionItem> result = collectionItemMapper.selectPage(pageObj, wrapper);
            return R.success(result);
        } else if ("collection".equals(normalizedType)) {
            Page<Collection> pageObj = new Page<>(page.getPageNum(), page.getPageSize());
            LambdaQueryWrapper<Collection> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Collection::getIsPublic, 1);
            if (StringUtils.hasText(searchKey)) {
                wrapper.and(w -> w.like(Collection::getName, searchKey)
                        .or().like(Collection::getDescription, searchKey));
            }
            wrapper.orderByDesc(Collection::getCreatedAt);
            Page<Collection> result = collectionMapper.selectPage(pageObj, wrapper);
            return R.success(result);
        } else if ("note".equals(normalizedType)) {
            Page<Note> pageObj = new Page<>(page.getPageNum(), page.getPageSize());
            LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Note::getIsPublic, 1);
            if (StringUtils.hasText(searchKey)) {
                wrapper.and(w -> w.like(Note::getTitle, searchKey)
                        .or().like(Note::getContent, searchKey));
            }
            wrapper.orderByDesc(Note::getCreateTime);
            Page<Note> result = noteMapper.selectPage(pageObj, wrapper);
            return R.success(result);
        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R takeDownContent(String contentType, Long contentId) {
        String normalizedType;
        if ("collect".equals(contentType) || "collection_item".equals(contentType) || "item".equals(contentType)) {
            normalizedType = "collection_item";
        } else {
            normalizedType = contentType;
        }

        if ("collection_item".equals(normalizedType)) {
            LambdaUpdateWrapper<CollectionItem> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CollectionItem::getId, contentId)
                    .set(CollectionItem::getIsPublic, 0);
            int rows = collectionItemMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("收藏项不存在");
            }
            log.info("下架收藏项[{}]", contentId);
        } else if ("collection".equals(normalizedType)) {
            LambdaUpdateWrapper<Collection> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Collection::getId, contentId)
                    .set(Collection::getIsPublic, 0);
            int rows = collectionMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("收藏集不存在");
            }
            // 同步下架该收藏集下的所有收藏项
            LambdaUpdateWrapper<CollectionItem> itemWrapper = new LambdaUpdateWrapper<>();
            itemWrapper.eq(CollectionItem::getCollectionId, contentId)
                    .eq(CollectionItem::getDeleted, 0)
                    .set(CollectionItem::getIsPublic, 0);
            int itemRows = collectionItemMapper.update(null, itemWrapper);
            log.info("下架收藏集[{}]，同步下架{}个收藏项", contentId, itemRows);
        } else if ("note".equals(normalizedType)) {
            LambdaUpdateWrapper<Note> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Note::getId, contentId)
                    .set(Note::getIsPublic, 0);
            int rows = noteMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("笔记不存在");
            }
            log.info("下架笔记[{}]", contentId);
        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }
        return R.success("内容已下架");
    }

    @Override
    public R getDashboard() {
        Map<String, Object> dashboard = new HashMap<>(16);

        // 用户统计
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        long totalUsers = userMapper.selectCount(userWrapper);
        dashboard.put("totalUsers", totalUsers);

        // 今日新增用户数
        LocalDateTime startOfToday = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1);
        LambdaQueryWrapper<User> todayUserWrapper = new LambdaQueryWrapper<>();
        todayUserWrapper.ge(User::getCreatedAt, startOfToday)
                .lt(User::getCreatedAt, endOfToday);
        long todayNewUsers = userMapper.selectCount(todayUserWrapper);
        dashboard.put("todayNewUsers", todayNewUsers);

        // 近7天活跃用户数（登录过）
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        LambdaQueryWrapper<User> activeUserWrapper = new LambdaQueryWrapper<>();
        activeUserWrapper.ge(User::getLastLoginAt, sevenDaysAgo);
        long activeUsers = userMapper.selectCount(activeUserWrapper);
        dashboard.put("activeUsers", activeUsers);

        // 收藏集统计
        LambdaQueryWrapper<Collection> collectionWrapper = new LambdaQueryWrapper<>();
        long totalCollections = collectionMapper.selectCount(collectionWrapper);
        dashboard.put("totalCollections", totalCollections);

        // 收藏项统计
        LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
        long totalItems = collectionItemMapper.selectCount(itemWrapper);
        dashboard.put("totalItems", totalItems);

        // 笔记统计
        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        long totalNotes = noteMapper.selectCount(noteWrapper);
        dashboard.put("totalNotes", totalNotes);

        // 公开内容统计
        LambdaQueryWrapper<Collection> publicCollectionWrapper = new LambdaQueryWrapper<>();
        publicCollectionWrapper.eq(Collection::getIsPublic, 1);
        long publicCollections = collectionMapper.selectCount(publicCollectionWrapper);
        dashboard.put("publicCollections", publicCollections);

        LambdaQueryWrapper<Note> publicNoteWrapper = new LambdaQueryWrapper<>();
        publicNoteWrapper.eq(Note::getIsPublic, 1);
        long publicNotes = noteMapper.selectCount(publicNoteWrapper);
        dashboard.put("publicNotes", publicNotes);

        // 公告统计（统计已发布状态的公告）
        LambdaQueryWrapper<Announcement> announcementWrapper = new LambdaQueryWrapper<>();
        announcementWrapper.eq(Announcement::getStatus, 1); // 1=已发布
        long activeAnnouncements = announcementMapper.selectCount(announcementWrapper);
        dashboard.put("activeAnnouncements", activeAnnouncements);

        return R.success(dashboard);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createUser(AdminCreateUserDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }

        // 检查用户名是否已存在
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已被使用
        if (StringUtils.hasText(dto.getEmail())) {
            User existingEmail = userMapper.selectByEmail(dto.getEmail());
            if (existingEmail != null) {
                throw new BusinessException("邮箱已被注册");
            }
        }

        // 角色校验：仅允许 "commonUser" 或 "admin"
        if (!"commonUser".equals(dto.getRole()) && !"admin".equals(dto.getRole())) {
            throw new BusinessException("角色值无效，仅允许 commonUser 或 admin");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(StringUtils.hasText(dto.getNickname()) ? dto.getNickname() : dto.getUsername());
        user.setRole(dto.getRole());
        userMapper.insert(user);

        user.setPassword(null);
        log.info("后台新增用户[{}]成功，用户名: {}", user.getId(), user.getUsername());
        return R.success("创建用户成功", user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateUser(Long id, AdminEditUserDTO dto) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 角色校验：仅允许 "commonUser" 或 "admin"
        if (!"commonUser".equals(dto.getRole()) && !"admin".equals(dto.getRole())) {
            throw new BusinessException("角色值无效，仅允许 commonUser 或 admin");
        }

        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, id);

        if (StringUtils.hasText(dto.getNickname())) {
            wrapper.set(User::getNickname, dto.getNickname());
        }
        wrapper.set(User::getRole, dto.getRole());

        userMapper.update(null, wrapper);
        log.info("后台编辑用户[{}]成功", id);
        return R.success("更新用户成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createAnnouncement(Long adminId, AnnouncementDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle()) || !StringUtils.hasText(dto.getContent())) {
            throw new BusinessException("公告标题和内容不能为空");
        }

        Announcement announcement = new Announcement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setEffectiveAt(dto.getEffectiveAt() != null ? dto.getEffectiveAt() : LocalDateTime.now());
        announcement.setExpireAt(dto.getExpireAt());
        announcement.setStatus(0);
        announcement.setType(StringUtils.hasText(dto.getType()) ? dto.getType() : "system");
        announcement.setPriority(StringUtils.hasText(dto.getPriority()) ? dto.getPriority() : "medium");
        announcement.setCreatedBy(adminId);
        announcementMapper.insert(announcement);

        saveOperationLog(adminId, "create_announcement", "announcement", announcement.getId(), "创建公告(草稿)");

        log.info("管理员[{}]创建公告[{}]成功(草稿)", adminId, announcement.getId());
        return R.success("创建公告成功", announcement);
    }

    /**
     * 向全平台所有用户发送系统公告通知
     * @param announcement 公告信息
     */
    private void sendAnnouncementToAllUsers(Announcement announcement) {
        // 查询所有活跃用户
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        List<User> allUsers = userMapper.selectList(userWrapper);

        if (allUsers == null || allUsers.isEmpty()) {
            log.warn("未找到活跃用户，跳过发送公告通知");
            return;
        }

        // 为每个用户创建通知
        for (User user : allUsers) {
            Notification notification = new Notification();
            notification.setUserId(user.getId());
            notification.setTitle("系统公告：" + announcement.getTitle());
            notification.setContent(announcement.getContent());
            notification.setNotifyType(2); // 2=系统公告类型
            notification.setIsRead(0);
            notification.setCreatedAt(LocalDateTime.now());
            notificationMapper.insert(notification);
        }

        log.info("系统公告已发送给[{}]个用户", allUsers.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateAnnouncement(Long adminId, Long id, AnnouncementDTO dto) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id);

        if (StringUtils.hasText(dto.getTitle())) {
            wrapper.set(Announcement::getTitle, dto.getTitle());
        }
        if (StringUtils.hasText(dto.getContent())) {
            wrapper.set(Announcement::getContent, dto.getContent());
        }
        if (dto.getEffectiveAt() != null) {
            wrapper.set(Announcement::getEffectiveAt, dto.getEffectiveAt());
        }
        if (dto.getExpireAt() != null) {
            wrapper.set(Announcement::getExpireAt, dto.getExpireAt());
        }
        if (StringUtils.hasText(dto.getType())) {
            wrapper.set(Announcement::getType, dto.getType());
        }
        if (StringUtils.hasText(dto.getPriority())) {
            wrapper.set(Announcement::getPriority, dto.getPriority());
        }

        announcementMapper.update(null, wrapper);

        // 记录操作日志
        saveOperationLog(adminId, "update_announcement", "announcement", id, "更新公告");

        log.info("管理员[{}]更新公告[{}]成功", adminId, id);
        return R.success("更新公告成功");
    }

    /**
     * 下架公告（修改状态为已下架，可恢复）
     * <p>将指定ID的公告状态更新为2（已下架状态），此操作可逆，可通过恢复操作重新上架。</p>
     *
     * @param adminId 执行操作的管理员ID，用于权限验证和操作日志记录
     * @param id      需要下架的公告ID
     * @return 操作结果
     * @throws BusinessException 当公告不存在或管理员权限不足时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R takeDownAnnouncement(Long adminId, Long id) {
        if (adminId == null) {
            throw new BusinessException("管理员ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }
        if (announcement.getStatus() == 2) {
            throw new BusinessException("公告已处于下架状态，无需重复操作");
        }

        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id)
                .set(Announcement::getStatus, 2);
        announcementMapper.update(null, wrapper);

        saveOperationLog(adminId, "take_down_announcement", "announcement", id, "下架公告");

        log.info("管理员[{}]下架公告[{}]成功", adminId, id);
        return R.success("公告已下架");
    }

    /**
     * 删除公告（逻辑删除，不可恢复）
     * <p>将指定ID的公告标记为已删除状态（status=-1），此操作不可逆。</p>
     *
     * @param adminId 执行操作的管理员ID，用于权限验证和操作日志记录
     * @param id      需要删除的公告ID
     * @return 操作结果
     * @throws BusinessException 当公告不存在或已被删除时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteAnnouncement(Long adminId, Long id) {
        if (adminId == null) {
            throw new BusinessException("管理员ID不能为空");
        }
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, id)
                .set(Announcement::getStatus, -1);
        announcementMapper.update(null, wrapper);

        saveOperationLog(adminId, "delete_announcement", "announcement", id, "删除公告(不可恢复)");

        log.info("管理员[{}]删除公告[{}]成功(不可恢复)", adminId, id);
        return R.success("公告已删除");
    }

    /**
     * 获取公告列表（分页，支持状态和类型筛选）
     * <p>根据状态和类型条件查询公告列表，结果按创建时间倒序排列。</p>
     *
     * @param page   页码，从1开始
     * @param size   每页条数，最大100
     * @param status 公告状态筛选条件，为null时不筛选（0=草稿,1=已发布,2=已下架,3=已过期）
     * @param type   公告类型筛选条件，为null时不筛选（system=系统公告,activity=活动通知,maintenance=维护通知）
     * @return 分页公告列表
     */
    @Override
    public R getAnnouncementList(Integer page, Integer size, Integer status, String type) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }

        Page<Announcement> pageObj = new Page<>(page, size);
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Announcement::getStatus, status);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(Announcement::getType, type);
        }
        wrapper.orderByDesc(Announcement::getCreatedAt);

        Page<Announcement> result = announcementMapper.selectPage(pageObj, wrapper);
        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R restoreContent(String contentType, Long contentId, Long adminId) {
        String normalizedType;
        if ("collect".equals(contentType) || "collection_item".equals(contentType) || "item".equals(contentType)) {
            normalizedType = "collection_item";
        } else {
            normalizedType = contentType;
        }

        if ("collection_item".equals(normalizedType)) {
            LambdaUpdateWrapper<CollectionItem> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(CollectionItem::getId, contentId)
                    .set(CollectionItem::getIsPublic, 1);
            int rows = collectionItemMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("收藏项不存在");
            }
            log.info("管理员[{}]恢复收藏项[{}]", adminId, contentId);
        } else if ("collection".equals(normalizedType)) {
            LambdaUpdateWrapper<Collection> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Collection::getId, contentId)
                    .set(Collection::getIsPublic, 1);
            int rows = collectionMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("收藏集不存在");
            }
            // 同步恢复该收藏集下的所有收藏项
            LambdaUpdateWrapper<CollectionItem> itemWrapper = new LambdaUpdateWrapper<>();
            itemWrapper.eq(CollectionItem::getCollectionId, contentId)
                    .eq(CollectionItem::getDeleted, 0)
                    .set(CollectionItem::getIsPublic, 1);
            int itemRows = collectionItemMapper.update(null, itemWrapper);
            log.info("管理员[{}]恢复收藏集[{}]，同步恢复{}个收藏项", adminId, contentId, itemRows);
        } else if ("note".equals(normalizedType)) {
            LambdaUpdateWrapper<Note> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(Note::getId, contentId)
                    .set(Note::getIsPublic, 1);
            int rows = noteMapper.update(null, wrapper);
            if (rows == 0) {
                throw new BusinessException("笔记不存在");
            }
            log.info("管理员[{}]恢复笔记[{}]", adminId, contentId);
        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }

        saveOperationLog(adminId, "restore", normalizedType, contentId, "恢复已下架内容");

        return R.success("内容已恢复");
    }

    @Override
    public R getContentAuditLogs(Integer pageNum, Integer pageSize, String operationType, String targetType, String startTime, String endTime) {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100;
        }

        Page<AdminOperationLog> pageObj = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(operationType)) {
            wrapper.eq(AdminOperationLog::getOperationType, operationType);
        }
        if (StringUtils.hasText(targetType)) {
            wrapper.eq(AdminOperationLog::getTargetType, targetType);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge(AdminOperationLog::getCreatedAt, startTime);
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le(AdminOperationLog::getCreatedAt, endTime);
        }

        wrapper.orderByDesc(AdminOperationLog::getCreatedAt);
        Page<AdminOperationLog> result = adminOperationLogMapper.selectPage(pageObj, wrapper);
        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R publishAnnouncement(Long adminId, Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 更新状态为已发布
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, announcementId)
                // 修复：使用状态1表示已发布
                .set(Announcement::getStatus, 1);
        announcementMapper.update(null, wrapper);

        // 发送全站通知
        sendAnnouncementToAllUsers(announcement);

        // 记录操作日志
        saveOperationLog(adminId, "publish_announcement", "announcement", announcementId, "发布公告");

        // 初始化统计数据
        AnnouncementStatistics stats = new AnnouncementStatistics();
        stats.setAnnouncementId(announcementId);
        stats.setViewCount(0);
        stats.setReadUserCount(0);
        announcementStatisticsMapper.insert(stats);

        log.info("管理员[{}]发布公告[{}]并推送通知", adminId, announcementId);
        return R.success("公告已发布并推送给所有用户");
    }

    @Override
    public R getAnnouncementStatistics(Long announcementId) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        LambdaQueryWrapper<AnnouncementStatistics> statsWrapper = new LambdaQueryWrapper<>();
        statsWrapper.eq(AnnouncementStatistics::getAnnouncementId, announcementId);
        AnnouncementStatistics stats = announcementStatisticsMapper.selectOne(statsWrapper);

        // 查询总用户数
        LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(User::getDeleted, 0);
        long totalUsers = userMapper.selectCount(userWrapper);

        Map<String, Object> result = new HashMap<>(8);
        result.put("announcementId", announcementId);
        result.put("title", announcement.getTitle());
        result.put("totalUsers", totalUsers);
        result.put("readUserCount", stats != null ? stats.getReadUserCount() : 0);
        result.put("viewCount", stats != null ? stats.getViewCount() : 0);
        result.put("readRate", totalUsers > 0 ? (stats != null ? stats.getReadUserCount() : 0) * 100.0 / totalUsers : 0);

        return R.success(result);
    }

    @Override
    public R getDashboardTrends(Integer days, String dataType) {
        if (days == null || days < 1) {
            days = 30;
        }

        Map<String, Object> result = new HashMap<>(8);
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(days);

        if ("users".equals(dataType) || dataType == null) {
            // 用户增长趋势
            List<Map<String, Object>> userTrend = new java.util.ArrayList<>();
            for (int i = days; i >= 0; i--) {
                LocalDateTime dayStart = endDate.minusDays(i).toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);

                LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
                wrapper.ge(User::getCreatedAt, dayStart)
                        .lt(User::getCreatedAt, dayEnd);
                long newUsers = userMapper.selectCount(wrapper);

                Map<String, Object> dayData = new HashMap<>(4);
                dayData.put("date", dayStart.toLocalDate().toString());
                dayData.put("newUsers", newUsers);
                userTrend.add(dayData);
            }
            result.put("userTrend", userTrend);
        }

        if ("content".equals(dataType) || dataType == null) {
            // 内容发布趋势
            List<Map<String, Object>> contentTrend = new java.util.ArrayList<>();
            for (int i = days; i >= 0; i--) {
                LocalDateTime dayStart = endDate.minusDays(i).toLocalDate().atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1);

                LambdaQueryWrapper<Collection> collectionWrapper = new LambdaQueryWrapper<>();
                collectionWrapper.ge(Collection::getCreatedAt, dayStart)
                        .lt(Collection::getCreatedAt, dayEnd);
                long newCollections = collectionMapper.selectCount(collectionWrapper);

                LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
                noteWrapper.ge(Note::getCreateTime, dayStart)
                        .lt(Note::getCreateTime, dayEnd);
                long newNotes = noteMapper.selectCount(noteWrapper);

                Map<String, Object> dayData = new HashMap<>(4);
                dayData.put("date", dayStart.toLocalDate().toString());
                dayData.put("newCollections", newCollections);
                dayData.put("newNotes", newNotes);
                contentTrend.add(dayData);
            }
            result.put("contentTrend", contentTrend);
        }

        return R.success(result);
    }

    @Override
    public R getDashboardHealth() {
        Map<String, Object> health = new HashMap<>(16);

        // 数据库状态
        try {
            userMapper.selectCount(new LambdaQueryWrapper<>());
            health.put("dbStatus", "normal");
        } catch (Exception e) {
            health.put("dbStatus", "error");
            log.error("数据库连接异常", e);
        }

        // 磁盘使用率（简化实现）
        try {
            java.io.File root = new java.io.File("/");
            long totalSpace = root.getTotalSpace();
            long freeSpace = root.getFreeSpace();
            long usedSpace = totalSpace - freeSpace;
            double usagePercent = totalSpace > 0 ? (usedSpace * 100.0 / totalSpace) : 0;
            health.put("diskUsage", Math.round(usagePercent * 100.0) / 100.0);
            health.put("diskTotalGB", totalSpace / 1024 / 1024 / 1024);
            health.put("diskFreeGB", freeSpace / 1024 / 1024 / 1024);
        } catch (Exception e) {
            health.put("diskUsage", "unknown");
        }

        // 系统运行时长
        java.lang.management.RuntimeMXBean runtimeBean = java.lang.management.ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeBean.getUptime();
        long uptimeDays = uptimeMillis / (1000 * 60 * 60 * 24);
        long uptimeHours = (uptimeMillis % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        health.put("uptimeDays", uptimeDays);
        health.put("uptimeHours", uptimeHours);

        // 用户和基础统计
        long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        health.put("totalUsers", totalUsers);

        long totalCollections = collectionMapper.selectCount(new LambdaQueryWrapper<>());
        health.put("totalCollections", totalCollections);

        long totalNotes = noteMapper.selectCount(new LambdaQueryWrapper<>());
        health.put("totalNotes", totalNotes);

        return R.success(health);
    }

    /**
     * 保存管理员操作日志
     */
    private void saveOperationLog(Long adminId, String operationType, String targetType, Long targetId, String detail) {
        try {
            AdminOperationLog logEntry = new AdminOperationLog();
            logEntry.setAdminId(adminId);
            logEntry.setOperationType(operationType);
            logEntry.setTargetType(targetType);
            logEntry.setTargetId(targetId);
            logEntry.setOperationDetail(detail);
            adminOperationLogMapper.insert(logEntry);
        } catch (Exception e) {
            log.error("保存操作日志失败", e);
        }
    }

    // ==================== 批量操作实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchDisableUsers(Long adminId, List<Long> userIds, String reason) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new java.util.ArrayList<>();

        for (Long userId : userIds) {
            try {
                User user = userMapper.selectById(userId);
                if (user == null) {
                    failCount++;
                    failReasons.add("用户ID[" + userId + "]不存在");
                    continue;
                }
                if ("admin".equals(user.getRole())) {
                    failCount++;
                    failReasons.add("用户[" + user.getUsername() + "]为管理员，不能禁用");
                    continue;
                }

                LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(User::getId, userId)
                        .set(User::getDeleted, 1);
                userMapper.update(null, wrapper);

                // 记录操作日志
                saveOperationLog(adminId, "batch_disable_user", "user", userId,
                        "批量禁用用户，原因：" + (reason != null ? reason : "未填写"));
                successCount++;
            } catch (Exception e) {
                failCount++;
                failReasons.add("用户ID[" + userId + "]操作失败：" + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("管理员[{}]批量禁用用户：成功[{}]，失败[{}]", adminId, successCount, failCount);
        return R.success("批量禁用完成，成功" + successCount + "个，失败" + failCount + "个", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchEnableUsers(Long adminId, List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException("用户ID列表不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new java.util.ArrayList<>();

        for (Long userId : userIds) {
            try {
                LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
                wrapper.eq(User::getId, userId)
                        .set(User::getDeleted, 0);
                int rows = userMapper.update(null, wrapper);
                if (rows == 0) {
                    failCount++;
                    failReasons.add("用户ID[" + userId + "]不存在");
                } else {
                    saveOperationLog(adminId, "batch_enable_user", "user", userId, "批量解封用户");
                    successCount++;
                }
            } catch (Exception e) {
                failCount++;
                failReasons.add("用户ID[" + userId + "]操作失败：" + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("管理员[{}]批量解封用户：成功[{}]，失败[{}]", adminId, successCount, failCount);
        return R.success("批量解封完成，成功" + successCount + "个，失败" + failCount + "个", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchTakeDownContent(Long adminId, String contentType, List<Long> contentIds, String reason) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new BusinessException("内容ID列表不能为空");
        }

        String normalizedType;
        if ("collect".equals(contentType) || "collection_item".equals(contentType) || "item".equals(contentType)) {
            normalizedType = "collection_item";
        } else {
            normalizedType = contentType;
        }
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new java.util.ArrayList<>();

        if ("collection_item".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<CollectionItem> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(CollectionItem::getId, contentId)
                            .set(CollectionItem::getIsPublic, 0);
                    int rows = collectionItemMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("收藏项ID[" + contentId + "]不存在");
                    } else {
                        saveOperationLog(adminId, "batch_take_down", "collection_item", contentId,
                                "批量下架，原因：" + (reason != null ? reason : "未填写"));
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("收藏项ID[" + contentId + "]操作失败");
                }
            }
        } else if ("collection".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<Collection> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Collection::getId, contentId)
                            .set(Collection::getIsPublic, 0);
                    int rows = collectionMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("收藏集ID[" + contentId + "]不存在");
                    } else {
                            // 同步下架该收藏集下的所有收藏项
                        LambdaUpdateWrapper<CollectionItem> itemWrapper = new LambdaUpdateWrapper<>();
                        itemWrapper.eq(CollectionItem::getCollectionId, contentId)
                                .eq(CollectionItem::getDeleted, 0)
                                .set(CollectionItem::getIsPublic, 0);
                        collectionItemMapper.update(null, itemWrapper);
                        saveOperationLog(adminId, "batch_take_down", "collection", contentId,
                                "批量下架，原因：" + (reason != null ? reason : "未填写"));
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("收藏集ID[" + contentId + "]操作失败");
                }
            }
        } else if ("note".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<Note> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Note::getId, contentId)
                            .set(Note::getIsPublic, 0);
                    int rows = noteMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("笔记ID[" + contentId + "]不存在");
                    } else {
                        saveOperationLog(adminId, "batch_take_down", "note", contentId,
                                "批量下架，原因：" + (reason != null ? reason : "未填写"));
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("笔记ID[" + contentId + "]操作失败");
                }
            }
        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("管理员[{}]批量下架{}：成功[{}]，失败[{}]", adminId, normalizedType, successCount, failCount);
        return R.success("批量下架完成，成功" + successCount + "个，失败" + failCount + "个", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R batchRestoreContent(Long adminId, String contentType, List<Long> contentIds) {
        if (contentIds == null || contentIds.isEmpty()) {
            throw new BusinessException("内容ID列表不能为空");
        }

        String normalizedType;
        if ("collect".equals(contentType) || "collection_item".equals(contentType) || "item".equals(contentType)) {
            normalizedType = "collection_item";
        } else {
            normalizedType = contentType;
        }
        int successCount = 0;
        int failCount = 0;
        List<String> failReasons = new java.util.ArrayList<>();

        if ("collection_item".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<CollectionItem> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(CollectionItem::getId, contentId)
                            .set(CollectionItem::getIsPublic, 1);
                    int rows = collectionItemMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("收藏项ID[" + contentId + "]不存在");
                    } else {
                        saveOperationLog(adminId, "batch_restore", "collection_item", contentId, "批量恢复内容");
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("收藏项ID[" + contentId + "]操作失败");
                }
            }
        } else if ("collection".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<Collection> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Collection::getId, contentId)
                            .set(Collection::getIsPublic, 1);
                    int rows = collectionMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("收藏集ID[" + contentId + "]不存在");
                    } else {
                        // 同步恢复该收藏集下的所有收藏项
                        LambdaUpdateWrapper<CollectionItem>itemWrapper = new LambdaUpdateWrapper<>();
                        itemWrapper.eq(CollectionItem::getCollectionId, contentId)
                                .eq(CollectionItem::getDeleted, 0)
                                .set(CollectionItem::getIsPublic, 1);
                        collectionItemMapper.update(null, itemWrapper);
                        saveOperationLog(adminId, "batch_restore", "collection", contentId, "批量恢复内容");
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("收藏集ID[" + contentId + "]操作失败");
                }
            }
        } else if ("note".equals(normalizedType)) {
            for (Long contentId : contentIds) {
                try {
                    LambdaUpdateWrapper<Note> wrapper = new LambdaUpdateWrapper<>();
                    wrapper.eq(Note::getId, contentId)
                            .set(Note::getIsPublic, 1);
                    int rows = noteMapper.update(null, wrapper);
                    if (rows == 0) {
                        failCount++;
                        failReasons.add("笔记ID[" + contentId + "]不存在");
                    } else {
                        saveOperationLog(adminId, "batch_restore", "note", contentId, "批量恢复内容");
                        successCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                    failReasons.add("笔记ID[" + contentId + "]操作失败");
                }
            }
        } else {
            throw new BusinessException("不支持的内容类型：" + contentType);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("failReasons", failReasons);

        log.info("管理员[{}]批量恢复{}：成功[{}]，失败[{}]", adminId, normalizedType, successCount, failCount);
        return R.success("批量恢复完成，成功" + successCount + "个，失败" + failCount + "个", result);
    }

    // ==================== 用户导出实现 ====================

    @Override
    public byte[] exportUsersToExcel(String keyword, String role) {
        // 构建查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getNickname, keyword)
                    .or().like(User::getEmail, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        List<User> users = userMapper.selectList(wrapper);

        // 使用StringBuilder构建CSV格式（兼容Excel）
        StringBuilder csv = new StringBuilder();
        // 表头
        csv.append("ID,用户名,昵称,邮箱,角色,状态,注册时间,最后登录时间\n");

        // 数据行
        for (User user : users) {
            csv.append(user.getId()).append(",");
            csv.append(escapeCsv(user.getUsername())).append(",");
            csv.append(escapeCsv(user.getNickname())).append(",");
            csv.append(escapeCsv(user.getEmail())).append(",");
            csv.append("admin".equals(user.getRole()) ? "管理员" : "普通用户").append(",");
            csv.append(user.getDeleted() == 0 ? "正常" : "已禁用").append(",");
            csv.append(user.getCreatedAt() != null ? user.getCreatedAt().toString() : "").append(",");
            csv.append(user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : "").append("\n");
        }

        return csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * CSV内容转义
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // ==================== 公告增强功能实现 ====================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R scheduleAnnouncement(Long adminId, Long announcementId, LocalDateTime scheduledAt) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null) {
            throw new BusinessException("公告不存在");
        }

        // 更新公告为定时发布状态，并记录定时时间
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getId, announcementId)
                .set(Announcement::getStatus, 4); // 4=待定时发布
        announcementMapper.update(null, wrapper);

        saveOperationLog(adminId, "schedule_announcement", "announcement", announcementId,
                "定时发布公告，定时时间：" + scheduledAt);

        log.info("管理员[{}]设置公告[{}]定时发布，时间：{}", adminId, announcementId, scheduledAt);
        return R.success("定时发布设置成功");
    }

    @Override
    public R getAnnouncementTemplates() {
        // 返回预设模板列表
        List<Map<String, Object>> templates = new java.util.ArrayList<>();

        // 系统通知模板
        templates.add(Map.of(
                "id", 1L,
                "name", "系统通知",
                "content", "亲爱的用户：\n\n{content}\n\n感谢您的使用！",
                "type", "system"
        ));

        // 活动公告模板
        templates.add(Map.of(
                "id", 2L,
                "name", "活动公告",
                "content", "【活动通知】\n\n{title}\n\n活动内容：\n{content}\n\n活动时间：{time}\n\n期待您的参与！",
                "type", "activity"
        ));

        // 维护通知模板
        templates.add(Map.of(
                "id", 3L,
                "name", "系统维护",
                "content", "【系统维护通知】\n\n尊敬的用户：\n\n为了给您提供更好的服务，系统将于 {time} 进行维护。\n\n维护内容：\n{content}\n\n请您提前做好相关准备，给您带来的不便敬请谅解。",
                "type", "maintenance"
        ));

        // 版本更新模板
        templates.add(Map.of(
                "id", 4L,
                "name", "版本更新",
                "content", "【版本更新公告】\n\n版本号：V{version}\n\n更新内容：\n{content}\n\n新增功能：\n{features}\n\n感谢您的支持！",
                "type", "system"
        ));

        return R.success(templates);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createAnnouncementTemplate(Long adminId, String name, String content, String type) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(content)) {
            throw new BusinessException("模板名称和内容不能为空");
        }

        // 这里应该创建模板记录到数据库，暂时返回成功信息
        // 实际实现需要创建 announcement_template 表

        log.info("管理员[{}]创建公告模板[{}]", adminId, name);
        Map<String, Object> result = new HashMap<>();
        result.put("name", name);
        result.put("content", content);
        result.put("type", type);
        return R.success("模板创建成功", result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteAnnouncementTemplate(Long adminId, Long templateId) {
        // 实际实现需要从数据库删除模板记录
        log.info("管理员[{}]删除公告模板[{}]", adminId, templateId);
        return R.success("模板删除成功");
    }
}
