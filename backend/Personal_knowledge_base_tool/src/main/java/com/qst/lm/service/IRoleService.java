package com.qst.lm.service;

import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.pojo.SysPermission;
import com.qst.lm.pojo.SysRole;

import java.util.List;

/**
 * 角色管理服务接口
 */
public interface IRoleService {

    /**
     * 获取角色列表（分页）
     * @param page 分页参数
     * @param keyword 搜索关键词
     * @return 角色列表
     */
    R getRoleList(PageDTO page, String keyword);

    /**
     * 获取角色详情
     * @param roleId 角色ID
     * @return 角色详情（含权限列表）
     */
    R getRoleDetail(Long roleId);

    /**
     * 创建角色
     * @param role 角色信息
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    R createRole(SysRole role, List<Long> permissionIds);

    /**
     * 更新角色
     * @param roleId 角色ID
     * @param role 角色信息
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    R updateRole(Long roleId, SysRole role, List<Long> permissionIds);

    /**
     * 删除角色（逻辑删除）
     * @param roleId 角色ID
     * @return 操作结果
     */
    R deleteRole(Long roleId);

    /**
     * 获取角色的权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    R<List<SysPermission>> getRolePermissions(Long roleId);

    /**
     * 为角色分配权限
     * @param roleId 角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    R assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取所有角色列表（不分页）
     * @return 角色列表
     */
    R<List<SysRole>> getAllRoles();
}
