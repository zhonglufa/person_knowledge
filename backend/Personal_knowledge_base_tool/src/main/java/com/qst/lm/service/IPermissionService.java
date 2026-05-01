package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.pojo.SysPermission;
import com.qst.lm.pojo.SysRole;

import java.util.List;

/**
 * 权限服务接口
 */
public interface IPermissionService {

    /**
     * 获取用户的权限码列表
     * @param userId 用户ID
     * @return 权限码列表
     */
    List<String> getUserPermissionCodes(Long userId);

    /**
     * 获取用户的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<SysRole> getUserRoles(Long userId);

    /**
     * 判断用户是否有指定权限
     * @param userId 用户ID
     * @param permissionCode 权限码
     * @return 是否有权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 判断用户是否有任意一个指定权限
     * @param userId 用户ID
     * @param permissionCodes 权限码数组
     * @return 是否有任意权限
     */
    boolean hasAnyPermission(Long userId, String... permissionCodes);

    /**
     * 判断用户是否有所有指定权限
     * @param userId 用户ID
     * @param permissionCodes 权限码数组
     * @return 是否有所有权限
     */
    boolean hasAllPermissions(Long userId, String... permissionCodes);

    /**
     * 获取权限树结构
     * @return 权限树
     */
    R<List<SysPermission>> getPermissionTree();

    /**
     * 获取所有权限列表
     * @return 权限列表
     */
    R<List<SysPermission>> getAllPermissions();

    /**
     * 分配角色给用户
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    R<String> assignRoleToUser(Long userId, Long roleId);

    /**
     * 移除用户的角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 操作结果
     */
    R<String> removeRoleFromUser(Long userId, Long roleId);

    /**
     * 清除用户的权限缓存
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);

    /**
     * 清除所有权限缓存
     */
    void clearAllPermissionCache();
}
