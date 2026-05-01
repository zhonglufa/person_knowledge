package com.qst.lm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.lm.common.R;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.SysPermissionMapper;
import com.qst.lm.mapper.SysRoleMapper;
import com.qst.lm.mapper.SysUserRoleMapper;
import com.qst.lm.pojo.SysPermission;
import com.qst.lm.pojo.SysRole;
import com.qst.lm.pojo.SysUserRole;
import com.qst.lm.service.IPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PermissionServiceImpl implements IPermissionService {

    private static final String PERMISSION_CACHE_KEY_PREFIX = "user:permissions:";
    private static final String ROLE_CACHE_KEY_PREFIX = "user:roles:";
    private static final long CACHE_EXPIRE_MINUTES = 30;

    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public PermissionServiceImpl(SysPermissionMapper permissionMapper,
                                 SysRoleMapper roleMapper,
                                 SysUserRoleMapper userRoleMapper,
                                 RedisTemplate<String, Object> redisTemplate,
                                 ObjectMapper objectMapper) {
        this.permissionMapper = permissionMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<String> getUserPermissionCodes(Long userId) {
        String cacheKey = PERMISSION_CACHE_KEY_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached instanceof String) {
            try {
                List<String> codes = objectMapper.readValue((String) cached,
                        new TypeReference<List<String>>() {});
                log.debug("从缓存获取用户[{}]权限列表, 数量[{}]", userId, codes.size());
                return codes;
            } catch (JsonProcessingException e) {
                log.warn("反序列化用户[{}]权限缓存失败: {}", userId, e.getMessage());
                redisTemplate.delete(cacheKey);
            }
        }

        log.debug("从数据库查询用户[{}]权限列表", userId);
        List<String> permissionCodes = permissionMapper.selectPermissionCodesByUserId(userId);

        try {
            String json = objectMapper.writeValueAsString(permissionCodes);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("序列化用户[{}]权限缓存失败: {}", userId, e.getMessage());
        }
        return permissionCodes;
    }

    @Override
    public List<SysRole> getUserRoles(Long userId) {
        String cacheKey = ROLE_CACHE_KEY_PREFIX + userId;
        Object cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached instanceof String) {
            try {
                List<SysRole> roles = objectMapper.readValue((String) cached,
                        new TypeReference<List<SysRole>>() {});
                log.debug("从缓存获取用户[{}]角色列表, 数量[{}]", userId, roles.size());
                return roles;
            } catch (JsonProcessingException e) {
                log.warn("反序列化用户[{}]角色缓存失败: {}", userId, e.getMessage());
                redisTemplate.delete(cacheKey);
            }
        }

        log.debug("从数据库查询用户[{}]角色列表", userId);
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);

        try {
            String json = objectMapper.writeValueAsString(roles);
            redisTemplate.opsForValue().set(cacheKey, json, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (JsonProcessingException e) {
            log.warn("序列化用户[{}]角色缓存失败: {}", userId, e.getMessage());
        }
        return roles;
    }

    @Override
    public boolean hasPermission(Long userId, String permissionCode) {
        if (userId == null || permissionCode == null) {
            return false;
        }
        List<String> permissionCodes = getUserPermissionCodes(userId);
        return permissionCodes.contains(permissionCode);
    }

    @Override
    public boolean hasAnyPermission(Long userId, String... permissionCodes) {
        if (userId == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        List<String> userPermissions = getUserPermissionCodes(userId);
        return Arrays.stream(permissionCodes).anyMatch(userPermissions::contains);
    }

    @Override
    public boolean hasAllPermissions(Long userId, String... permissionCodes) {
        if (userId == null || permissionCodes == null || permissionCodes.length == 0) {
            return false;
        }
        List<String> userPermissions = getUserPermissionCodes(userId);
        return Arrays.stream(permissionCodes).allMatch(userPermissions::contains);
    }

    @Override
    public R<List<SysPermission>> getPermissionTree() {
        List<SysPermission> allPermissions = permissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .eq(SysPermission::getDeleted, 0)
                        .orderByAsc(SysPermission::getModule)
                        .orderByAsc(SysPermission::getSortNo)
        );

        Map<String, List<SysPermission>> moduleGroups = allPermissions.stream()
                .collect(Collectors.groupingBy(SysPermission::getModule));

        List<SysPermission> tree = new ArrayList<>();
        moduleGroups.forEach((module, permissions) -> {
            SysPermission moduleNode = new SysPermission();
            moduleNode.setPermissionCode(module);
            moduleNode.setPermissionName(getModuleName(module));
            moduleNode.setPermissionType("module");
            moduleNode.setChildren(permissions);
            tree.add(moduleNode);
        });

        return R.success(tree);
    }

    @Override
    public R<List<SysPermission>> getAllPermissions() {
        List<SysPermission> permissions = permissionMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SysPermission>()
                        .eq(SysPermission::getStatus, 1)
                        .eq(SysPermission::getDeleted, 0)
                        .orderByAsc(SysPermission::getModule)
                        .orderByAsc(SysPermission::getSortNo)
        );
        return R.success(permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> assignRoleToUser(Long userId, Long roleId) {
        int count = userRoleMapper.countByUserIdAndRoleId(userId, roleId);
        if (count > 0) {
            return R.error("用户已拥有该角色");
        }

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);

        clearUserPermissionCache(userId);
        log.info("为用户[{}]分配角色[{}]", userId, roleId);
        return R.success("角色分配成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> removeRoleFromUser(Long userId, Long roleId) {
        int count = userRoleMapper.deleteByUserIdAndRoleId(userId, roleId);
        if (count == 0) {
            return R.error("用户未拥有该角色");
        }

        clearUserPermissionCache(userId);
        log.info("移除用户[{}]的角色[{}]", userId, roleId);
        return R.success("角色移除成功");
    }

    @Override
    public void clearUserPermissionCache(Long userId) {
        String permissionKey = PERMISSION_CACHE_KEY_PREFIX + userId;
        String roleKey = ROLE_CACHE_KEY_PREFIX + userId;
        redisTemplate.delete(permissionKey);
        redisTemplate.delete(roleKey);
        log.debug("清除用户[{}]权限缓存", userId);
    }

    @Override
    public void clearAllPermissionCache() {
        Set<String> keys = redisTemplate.keys(PERMISSION_CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        Set<String> roleKeys = redisTemplate.keys(ROLE_CACHE_KEY_PREFIX + "*");
        if (roleKeys != null && !roleKeys.isEmpty()) {
            redisTemplate.delete(roleKeys);
        }
        log.info("清除所有权限缓存");
    }

    private String getModuleName(String module) {
        Map<String, String> moduleNames = Map.of(
                "admin", "后台管理",
                "user", "用户管理",
                "content", "内容管理",
                "announcement", "公告管理",
                "taxonomy", "分类标签"
        );
        return moduleNames.getOrDefault(module, module);
    }
}
