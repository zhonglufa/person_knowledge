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
    private final RedisTemplate<String, Object> redisTemplate;
    private final TokenBlacklistService tokenBlacklistService;

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
                           RedisTemplate<String, Object> redisTemplate,
                           TokenBlacklistService tokenBlacklistService) {
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
        this.redisTemplate = redisTemplate;
        this.tokenBlacklistService = tokenBlacklistService;
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
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            String failKey = "login:fail:" + dto.getUsername();
            redisTemplate.opsForValue().increment(failKey);
            redisTemplate.expire(failKey, 10, TimeUnit.MINUTES);
            Object failVal = redisTemplate.opsForValue().get(failKey);
            long failCount = failVal instanceof String ? Long.parseLong((String) failVal) : ((Number) failVal).longValue();
            if (failCount >= 5) {
                redisTemplate.opsForValue().set(lockKey, 1, 10, TimeUnit.MINUTES);
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
        loginData.put("userInfo", user);
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
        defaultCategory.setParentId(0L);
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
        return R.success(user);
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
        Map<String, Object> stats = new HashMap<>(8);

        LambdaQueryWrapper<Collection> collWrapper = new LambdaQueryWrapper<>();
        collWrapper.eq(Collection::getUserId, userId);
        Long collectionCount = collectionMapper.selectCount(collWrapper);
        stats.put("collectionCount", collectionCount);

        LambdaQueryWrapper<CollectionItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(CollectionItem::getUserId, userId);
        Long itemCount = collectionItemMapper.selectCount(itemWrapper);
        stats.put("collectionItemCount", itemCount);

        LambdaQueryWrapper<Note> noteWrapper = new LambdaQueryWrapper<>();
        noteWrapper.eq(Note::getUserId, userId);
        Long noteCount = noteMapper.selectCount(noteWrapper);
        stats.put("noteCount", noteCount);

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

        return R.success(stats);
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
