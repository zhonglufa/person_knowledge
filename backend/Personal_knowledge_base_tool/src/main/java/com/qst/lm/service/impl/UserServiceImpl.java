package com.qst.lm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.qst.lm.common.R;
import com.qst.lm.dto.auth.ChangePasswordDTO;
import com.qst.lm.dto.auth.LoginDTO;
import com.qst.lm.dto.auth.RegisterDTO;
import com.qst.lm.dto.settings.UserSettingsDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.*;
import com.qst.lm.pojo.*;
import com.qst.lm.pojo.SysRole;
import com.qst.lm.service.IPermissionService;
import com.qst.lm.service.IUserService;
import com.qst.lm.service.TokenBlacklistService;
import com.qst.lm.service.VerificationCodeService;
import com.qst.lm.utils.JwtUtils;
import com.qst.lm.utils.OssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    private final UserMapper userMapper;
    private final CollectionMapper collectionMapper;
    private final CollectionItemMapper collectionItemMapper;
    private final NoteMapper noteMapper;
    private final NotificationMapper notificationMapper;
    private final VerificationCodeService verificationCodeService;
    private final OssUtil ossUtil;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserSettingsMapper userSettingsMapper;
    private final ObjectMapper objectMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenBlacklistService tokenBlacklistService;
    private final IPermissionService permissionService;
    private final SysRoleMapper sysRoleMapper;

    public UserServiceImpl(UserMapper userMapper,
                           CollectionMapper collectionMapper,
                           CollectionItemMapper collectionItemMapper,
                           NoteMapper noteMapper,
                           NotificationMapper notificationMapper,
                           VerificationCodeService verificationCodeService,
                           OssUtil ossUtil,
                           JwtUtils jwtUtils,
                           UserSettingsMapper userSettingsMapper,
                           CategoryMapper categoryMapper,
                           TagMapper tagMapper,
                           RedisTemplate<String, Object> redisTemplate,
                           TokenBlacklistService tokenBlacklistService,
                           IPermissionService permissionService,
                           SysRoleMapper sysRoleMapper) {
        this.userMapper = userMapper;
        this.collectionMapper = collectionMapper;
        this.collectionItemMapper = collectionItemMapper;
        this.noteMapper = noteMapper;
        this.notificationMapper = notificationMapper;
        this.verificationCodeService = verificationCodeService;
        this.ossUtil = ossUtil;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.userSettingsMapper = userSettingsMapper;
        this.objectMapper = new ObjectMapper();
        this.categoryMapper = categoryMapper;
        this.tagMapper = tagMapper;
        this.redisTemplate = redisTemplate;
        this.tokenBlacklistService = tokenBlacklistService;
        this.permissionService = permissionService;
        this.sysRoleMapper = sysRoleMapper;
    }

    @Override
    public R login(LoginDTO dto) {
        String lockKey = "login:lockout:" + dto.getUsername();
        Boolean isLocked = redisTemplate.hasKey(lockKey);
        if (Boolean.TRUE.equals(isLocked)) {
            throw new BusinessException("账号已锁定，请10分钟后重试");
        }

        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            user = userMapper.selectByEmail(dto.getUsername());
        }
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new BusinessException("账号已被禁用");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            String failKey = "login:fail:" + dto.getUsername();
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, 10, TimeUnit.MINUTES);
            Object failVal = redisTemplate.opsForValue().get(failKey);
            long failCount = failVal instanceof String ? Long.parseLong((String) failVal) : ((Number) failVal).longValue();
            if (failCount >= 5) {
                redisTemplate.opsForValue().set(lockKey, "1", 10, TimeUnit.MINUTES);
                throw new BusinessException("密码错误次数过多，账号已锁定10分钟");
            }
            throw new BusinessException("用户名或密码错误");
        }

        if (!"enabled".equals(user.getStatus())) {
            throw new BusinessException("账号已被禁用");
        }

        redisTemplate.delete("login:fail:" + dto.getUsername());
        redisTemplate.delete(lockKey);

        Long tokenVersion = user.getTokenVersion() == null ? 1L : user.getTokenVersion();
        String token = jwtUtils.generateToken(user.getUsername(), user.getId(), tokenVersion);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .set(User::getLastLoginAt, LocalDateTime.now());
        userMapper.update(null, updateWrapper);
        user.setLastLoginAt(LocalDateTime.now());

        Map<String, Object> loginData = new HashMap<>(2);
        loginData.put("token", token);

        user.setPassword(null);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("gender", user.getGender());
        userInfo.put("bio", user.getBio());
        userInfo.put("expertise", user.getExpertise());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("deleted", user.getDeleted());
        userInfo.put("collectionCount", user.getCollectionCount());
        userInfo.put("collectionItemCount", user.getCollectionItemCount());
        userInfo.put("noteCount", user.getNoteCount());
        userInfo.put("unreadNotifyCount", user.getUnreadNotifyCount());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        userInfo.put("tokenVersion", user.getTokenVersion());
        userInfo.put("status", user.getStatus());

        userInfo.put("role", user.getRole());
        List<String> userRoles = permissionService.getUserRoles(user.getId()).stream().map(SysRole::getRoleCode).toList();
        if (userRoles.isEmpty() && StringUtils.hasText(user.getRole())) {
            SysRole fallbackRole = sysRoleMapper.selectByRoleCode("commonUser".equals(user.getRole()) ? "common_user" : user.getRole());
            if (fallbackRole != null) {
                userRoles = java.util.List.of(fallbackRole.getRoleCode());
            }
        }
        userInfo.put("roles", userRoles);
        userInfo.put("permissions", permissionService.getUserPermissionCodes(user.getId()));

        loginData.put("userInfo", userInfo);
        log.info("用户[{}]登录成功", user.getId());
        return R.success("登录成功", loginData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R register(RegisterDTO dto) {
        User existingUser = userMapper.selectByUsername(dto.getUsername());
        if (existingUser != null) {
            throw new BusinessException("用户名已存在");
        }
        User existingEmail = userMapper.selectByEmail(dto.getEmail());
        if (existingEmail != null) {
            throw new BusinessException("邮箱已被注册");
        }
        if (!verificationCodeService.verifyCode(dto.getEmail(), dto.getCode())) {
            throw new BusinessException("验证码无效或已过期");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setNickname(dto.getUsername());
        user.setRole("commonUser");
        user.setTokenVersion(1L);
        user.setStatus("enabled");
        userMapper.insert(user);

        Category defaultCategory = new Category();
        defaultCategory.setUserId(user.getId());
        defaultCategory.setName("默认分类");
        defaultCategory.setParentId(null);
        categoryMapper.insert(defaultCategory);

        Collection defaultCollection = new Collection();
        defaultCollection.setUserId(user.getId());
        defaultCollection.setName("默认收藏集");
        defaultCollection.setIsDefault(1);
        collectionMapper.insert(defaultCollection);

        log.info("用户[{}]注册成功", user.getId());
        return R.success("注册成功");
    }

    @Override
    public R logout(String authorization) {
        String token = jwtUtils.extractToken(authorization);
        if (token == null || token.isBlank()) {
            return R.success("登出成功");
        }
        try {
            long ttlMillis = jwtUtils.getRemainingExpiration(token);
            tokenBlacklistService.blacklistToken(token, ttlMillis);
        } catch (Exception e) {
            log.warn("登出时加入黑名单失败：{}", e.getMessage());
        }
        return R.success("登出成功");
    }

    @Override
    public R getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("phone", user.getPhone());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("gender", user.getGender());
        userInfo.put("bio", user.getBio());
        userInfo.put("expertise", user.getExpertise());
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("updatedAt", user.getUpdatedAt());
        userInfo.put("deleted", user.getDeleted());
        userInfo.put("collectionCount", user.getCollectionCount());
        userInfo.put("collectionItemCount", user.getCollectionItemCount());
        userInfo.put("noteCount", user.getNoteCount());
        userInfo.put("unreadNotifyCount", user.getUnreadNotifyCount());
        userInfo.put("lastLoginAt", user.getLastLoginAt());
        userInfo.put("tokenVersion", user.getTokenVersion());
        userInfo.put("status", user.getStatus());
        userInfo.put("role", user.getRole());
        List<String> userRoles = permissionService.getUserRoles(user.getId()).stream().map(SysRole::getRoleCode).toList();
        if (userRoles.isEmpty() && StringUtils.hasText(user.getRole())) {
            SysRole fallbackRole = sysRoleMapper.selectByRoleCode("commonUser".equals(user.getRole()) ? "common_user" : user.getRole());
            if (fallbackRole != null) {
                userRoles = java.util.List.of(fallbackRole.getRoleCode());
            }
        }
        userInfo.put("roles", userRoles);
        userInfo.put("permissions", permissionService.getUserPermissionCodes(user.getId()));

        return R.success(userInfo);
    }

    @Override
    public R updatePassword(Long userId, ChangePasswordDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId)
                .set(User::getPassword, passwordEncoder.encode(dto.getNewPassword()))
                .setSql("token_version = IFNULL(token_version, 1) + 1");
        userMapper.update(null, wrapper);
        log.info("用户[{}]修改密码成功", userId);
        return R.success("密码修改成功，请重新登录");
    }

    @Override
    public R getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return R.success(user);
    }

    @Override
    public R updateProfile(Long userId, Map<String, Object> profileMap) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, userId);

        if (profileMap.containsKey("nickname")) {
            String nickname = (String) profileMap.get("nickname");
            if (StringUtils.hasText(nickname)) {
                wrapper.set(User::getNickname, nickname);
            }
        }
        if (profileMap.containsKey("email")) {
            String email = (String) profileMap.get("email");
            if (StringUtils.hasText(email)) {
                User emailUser = userMapper.selectByEmail(email);
                if (emailUser != null && !emailUser.getId().equals(userId)) {
                    throw new BusinessException("该邮箱已被其他用户使用");
                }
                wrapper.set(User::getEmail, email);
            }
        }
        if (profileMap.containsKey("avatar")) {
            wrapper.set(User::getAvatar, (String) profileMap.get("avatar"));
        }
        if (profileMap.containsKey("phone")) {
            wrapper.set(User::getPhone, (String) profileMap.get("phone"));
        }
        if (profileMap.containsKey("gender")) {
            wrapper.set(User::getGender, (String) profileMap.get("gender"));
        }
        if (profileMap.containsKey("bio")) {
            wrapper.set(User::getBio, (String) profileMap.get("bio"));
        }
        if (profileMap.containsKey("expertise")) {
            Object expertise = profileMap.get("expertise");
            if (expertise != null) {
                try {
                    wrapper.set(User::getExpertise, objectMapper.writeValueAsString(expertise));
                } catch (JsonProcessingException e) {
                    throw new BusinessException("专业领域格式不正确");
                }
            } else {
                wrapper.set(User::getExpertise, null);
            }
        }

        userMapper.update(null, wrapper);
        log.info("用户[{}]更新个人信息成功", userId);
        return R.success("个人信息更新成功");
    }

    @Override
    public R uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择头像文件");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片文件");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new BusinessException("头像文件大小不能超过5MB");
        }
        try {
            String avatarUrl = ossUtil.uploadFile(file, true);
            userMapper.updateAvatar(userId, avatarUrl);
            log.info("用户[{}]上传头像成功: {}", userId, avatarUrl);
            return R.success("头像上传成功", avatarUrl);
        } catch (Exception e) {
            log.error("用户[{}]上传头像失败", userId, e);
            throw new BusinessException("头像上传失败，请稍后重试");
        }
    }

    @Override
    public R getStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>(16);

        LambdaQueryWrapper<Collection> collWrapper = new LambdaQueryWrapper<>();
        collWrapper.eq(Collection::getUserId, userId);
        Long collectionCount = collectionMapper.selectCount(collWrapper);
        stats.put("collectionCount", collectionCount);
        stats.put("totalCollections", collectionCount);

        LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(CollectionItem::getUserId, userId);
        Long itemCount = collectionItemMapper.selectCount(itemWrapper);
        stats.put("collectionItemCount", itemCount);

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId);
        Long noteCount = noteMapper.selectCount(noteWrapper);
        stats.put("noteCount", noteCount);
        stats.put("totalNotes", noteCount);
        
        LambdaQueryWrapper<Note> publicNoteWrapper = new LambdaQueryWrapper<>();
        publicNoteWrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsPublic, 1);
        Long publicContent = noteMapper.selectCount(publicNoteWrapper);
        stats.put("publicContent", publicContent);

        LambdaQueryWrapper<Notification> notifyWrapper = new LambdaQueryWrapper<>();
        notifyWrapper.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        Long unreadCount = notificationMapper.selectCount(notifyWrapper);
        stats.put("unreadNotifyCount", unreadCount);

        Map<String, Long> digestStats = new HashMap<>(4);
        for (String status : new String[]{"undigest", "digesting", "digested", "abandoned"}) {
            LambdaQueryWrapper<CollectionItem> dWrapper = new LambdaQueryWrapper<>();
            dWrapper.eq(CollectionItem::getUserId, userId)
                    .eq(CollectionItem::getDigestStatus, status);
            Long count = collectionItemMapper.selectCount(dWrapper);
            digestStats.put(status, count);
        }
        stats.put("digestStats", digestStats);
        
        Long processingItems = digestStats.getOrDefault("digesting", 0L);
        stats.put("processingItems", processingItems);
        
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        tagWrapper.eq(Tag::getUserId, userId);
        Long tagCount = tagMapper.selectCount(tagWrapper);
        stats.put("totalTags", tagCount);
        
        LambdaQueryWrapper<Note> originalWrapper = new LambdaQueryWrapper<>();
        originalWrapper.eq(Note::getUserId, userId)
                .eq(Note::getNoteType, "conceptual");
        Long noteOriginal = noteMapper.selectCount(originalWrapper);
        
        LambdaQueryWrapper<Note> summaryWrapper = new LambdaQueryWrapper<>();
        summaryWrapper.eq(Note::getUserId, userId)
                .eq(Note::getNoteType, "procedural");
        Long noteSummary = noteMapper.selectCount(summaryWrapper);
        
        LambdaQueryWrapper<Note> normalWrapper = new LambdaQueryWrapper<>();
        normalWrapper.eq(Note::getUserId, userId)
                .in(Note::getNoteType, "factual", "metacognitive");
        Long noteNormal = noteMapper.selectCount(normalWrapper);
        
        stats.put("noteOriginal", noteOriginal);
        stats.put("noteSummary", noteSummary);
        stats.put("noteNormal", noteNormal);
        
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime todayStart = today.atStartOfDay();
        java.time.LocalDateTime weekStart = today.minusDays(7).atStartOfDay();
        java.time.LocalDateTime monthStart = today.minusDays(30).atStartOfDay();
        java.time.LocalDateTime lastWeekStart = today.minusDays(14).atStartOfDay();
        
        LambdaQueryWrapper<CollectionItem> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(CollectionItem::getUserId, userId)
                .ge(CollectionItem::getCreatedAt, todayStart);
        Long todayNew = collectionItemMapper.selectCount(todayWrapper);
        stats.put("todayNew", todayNew);
        
        LambdaQueryWrapper<CollectionItem> weekWrapper = new LambdaQueryWrapper<>();
        weekWrapper.eq(CollectionItem::getUserId, userId)
                .ge(CollectionItem::getCreatedAt, weekStart);
        Long weekNew = collectionItemMapper.selectCount(weekWrapper);
        stats.put("weekNew", weekNew);
        
        LambdaQueryWrapper<CollectionItem> lastWeekWrapper = new LambdaQueryWrapper<>();
        lastWeekWrapper.eq(CollectionItem::getUserId, userId)
                .ge(CollectionItem::getCreatedAt, lastWeekStart)
                .lt(CollectionItem::getCreatedAt, weekStart);
        Long lastWeekNew = collectionItemMapper.selectCount(lastWeekWrapper);
        
        double collectionTrend = lastWeekNew > 0 ? ((weekNew - lastWeekNew) * 100.0 / lastWeekNew) : (weekNew > 0 ? 100.0 : 0.0);
        stats.put("collectionTrend", Math.round(collectionTrend * 10) / 10.0);
        
        LambdaQueryWrapper<Note> weekNoteWrapper = new LambdaQueryWrapper<>();
        weekNoteWrapper.eq(Note::getUserId, userId)
                .ge(Note::getCreateTime, weekStart);
        Long weekNoteNew = noteMapper.selectCount(weekNoteWrapper);
        
        LambdaQueryWrapper<Note> lastWeekNoteWrapper = new LambdaQueryWrapper<>();
        lastWeekNoteWrapper.eq(Note::getUserId, userId)
                .ge(Note::getCreateTime, lastWeekStart)
                .lt(Note::getCreateTime, weekStart);
        Long lastWeekNoteNew = noteMapper.selectCount(lastWeekNoteWrapper);
        
        double noteTrend = lastWeekNoteNew > 0 ? ((weekNoteNew - lastWeekNoteNew) * 100.0 / lastWeekNoteNew) : (weekNoteNew > 0 ? 100.0 : 0.0);
        stats.put("noteTrend", Math.round(noteTrend * 10) / 10.0);
        
        Long currentProcessing = digestStats.getOrDefault("digesting", 0L);
        stats.put("processingTrend", 0.0);
        
        LambdaQueryWrapper<CollectionItem> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(CollectionItem::getUserId, userId)
                .ge(CollectionItem::getCreatedAt, monthStart);
        Long monthNew = collectionItemMapper.selectCount(monthWrapper);
        stats.put("monthNew", monthNew);
        
        stats.put("streakDays", 0);

        Long totalItems = itemCount;
        Long digestedItems = digestStats.getOrDefault("digested", 0L);
        int learningProgress = totalItems > 0 ? (int) ((digestedItems * 100) / totalItems) : 0;
        stats.put("learningProgress", learningProgress);

        stats.put("learningTrend", 5.8);

        Map<String, Long> learningStats = new HashMap<>(3);
        learningStats.put("completed", digestedItems);
        learningStats.put("inProgress", digestStats.getOrDefault("digesting", 0L));
        learningStats.put("overdue", 0L);
        stats.put("learningStats", learningStats);

        List<Map<String, Object>> learningProgressList = new java.util.ArrayList<>();
        List<CollectionItem> recentItems = collectionItemMapper.selectList(
            new LambdaQueryWrapper<CollectionItem>()
                .eq(CollectionItem::getUserId, userId)
                .orderByDesc(CollectionItem::getUpdatedAt)
                .last("LIMIT 5")
        );
        for (CollectionItem item : recentItems) {
            Map<String, Object> progressItem = new HashMap<>(4);
            progressItem.put("title", item.getTitle());
            int progress = 0;
            String status = "in_progress";
            if ("digested".equals(item.getDigestStatus())) {
                progress = 100;
                status = "completed";
            } else if ("digesting".equals(item.getDigestStatus())) {
                progress = 50;
                status = "in_progress";
            } else if ("undigest".equals(item.getDigestStatus())) {
                progress = 10;
                status = "in_progress";
            }
            progressItem.put("progress", progress);
            progressItem.put("status", status);
            learningProgressList.add(progressItem);
        }
        stats.put("learningProgressList", learningProgressList);
        
        List<Map<String, Object>> recentActivities = new java.util.ArrayList<>();
        List<CollectionItem> recentActivityItems = collectionItemMapper.selectList(
            new LambdaQueryWrapper<CollectionItem>()
                .eq(CollectionItem::getUserId, userId)
                .orderByDesc(CollectionItem::getCreatedAt)
                .last("LIMIT 4")
        );
        for (CollectionItem item : recentActivityItems) {
            Map<String, Object> activity = new HashMap<>(6);
            activity.put("id", item.getId());
            activity.put("type", "primary");
            activity.put("icon", "fas fa-bookmark");
            activity.put("title", "新增收藏");
            activity.put("description", "收藏了 \"" + item.getTitle() + "\"");
            activity.put("time", item.getCreatedAt());
            activity.put("targetId", item.getId());
            activity.put("targetType", "collection");
            recentActivities.add(activity);
        }
        stats.put("recentActivities", recentActivities);

        long[] typeDistribution = new long[5];
        for (int i = 1; i <= 5; i++) {
            LambdaQueryWrapper<CollectionItem> typeWrapper = new LambdaQueryWrapper<>();
            typeWrapper.eq(CollectionItem::getUserId, userId)
                    .eq(CollectionItem::getSourceType, i);
            typeDistribution[i - 1] = collectionItemMapper.selectCount(typeWrapper);
        }
        stats.put("typeDistribution", typeDistribution);

        long[] processingDistribution = new long[4];
        String[] statuses = {"undigest", "digesting", "digested", "abandoned"};
        for (int i = 0; i < statuses.length; i++) {
            processingDistribution[i] = digestStats.getOrDefault(statuses[i], 0L);
        }
        stats.put("processingDistribution", processingDistribution);

        return R.success(stats);
    }

    @Override
    public R sendVerifyCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new BusinessException("邮箱不能为空");
        }
        if (!verificationCodeService.canSendCode(email)) {
            throw new BusinessException("发送过于频繁，请稍后再试");
        }
        String code = verificationCodeService.generateCode();
        System.out.println("发送验证码成功，邮箱: {}"+ email+"验证码"+code);
        verificationCodeService.sendVerificationCode(email, code);
        verificationCodeService.saveCode(email, code);
        log.info("发送验证码成功，邮箱: {}", email);
        return R.success("验证码发送成功");
    }

    @Override
    public R resetPassword(String email, String code, String newPassword) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(code) || !StringUtils.hasText(newPassword)) {
            throw new BusinessException("参数不完整");
        }
        if (!verificationCodeService.verifyCode(email, code)) {
            throw new BusinessException("验证码无效或已过期");
        }
        User user = userMapper.selectByEmail(email);
        if (user == null) {
            throw new BusinessException("该邮箱未注册");
        }
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(User::getId, user.getId())
                .set(User::getPassword, passwordEncoder.encode(newPassword))
                .setSql("token_version = IFNULL(token_version, 1) + 1");
        userMapper.update(null, wrapper);
        log.info("用户[{}]重置密码成功", user.getId());
        return R.success("密码重置成功，请重新登录");
    }

    @Override
    public R getSettings(Long userId) {
        UserSettings settings = userSettingsMapper.selectByUserId(userId);

        if (settings == null) {
            Map<String, Object> defaultSettings = new HashMap<>(8);
            defaultSettings.put("theme", "light");
            defaultSettings.put("displayMode", "card");
            defaultSettings.put("notifyPreferences", "{\"email\":true,\"system\":true}");
            return R.success(defaultSettings);
        }
        Map<String, Object> result = new HashMap<>(8);
        result.put("theme", settings.getTheme());
        result.put("displayMode", settings.getDisplayMode());
        result.put("notifyPreferences", settings.getNotifyPreferences());
        return R.success(result);
    }

    @Override
    public R updateSettings(Long userId, UserSettingsDTO dto) {
        UserSettings settings = userSettingsMapper.selectByUserId(userId);
        if (settings == null) {
            settings = new UserSettings();
            settings.setUserId(userId);
            settings.setTheme(dto.getTheme());
            settings.setDisplayMode(dto.getDisplayMode());
            try {
                settings.setNotifyPreferences(objectMapper.writeValueAsString(dto.getNotifyPreferences()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("通知偏好格式错误");
            }
            userSettingsMapper.insert(settings);
        } else {
            LambdaUpdateWrapper<UserSettings> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(UserSettings::getUserId, userId)
                    .set(UserSettings::getTheme, dto.getTheme())
                    .set(UserSettings::getDisplayMode, dto.getDisplayMode());
            try {
                wrapper.set(UserSettings::getNotifyPreferences, objectMapper.writeValueAsString(dto.getNotifyPreferences()));
            } catch (JsonProcessingException e) {
                throw new BusinessException("通知偏好格式错误");
            }
            userSettingsMapper.update(null, wrapper);
        }
        log.info("用户[{}]更新个性化设置成功", userId);
        return R.success("设置更新成功");
    }
}
