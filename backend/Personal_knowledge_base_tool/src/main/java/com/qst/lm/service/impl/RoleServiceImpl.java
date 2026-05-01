package com.qst.lm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.exception.BusinessException;
import com.qst.lm.mapper.SysPermissionMapper;
import com.qst.lm.mapper.SysRoleMapper;
import com.qst.lm.mapper.SysRolePermissionMapper;
import com.qst.lm.mapper.SysUserRoleMapper;
import com.qst.lm.pojo.SysPermission;
import com.qst.lm.pojo.SysRole;
import com.qst.lm.pojo.SysRolePermission;
import com.qst.lm.service.IPermissionService;
import com.qst.lm.service.IRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoleServiceImpl implements IRoleService {

    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final IPermissionService permissionService;

    public RoleServiceImpl(SysRoleMapper roleMapper,
                           SysPermissionMapper permissionMapper,
                           SysRolePermissionMapper rolePermissionMapper,
                           SysUserRoleMapper userRoleMapper,
                           IPermissionService permissionService) {
        this.roleMapper = roleMapper;
        this.permissionMapper = permissionMapper;
        this.rolePermissionMapper = rolePermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionService = permissionService;
    }

    @Override
    public R getRoleList(PageDTO page, String keyword) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleCode, keyword)
                    .or().like(SysRole::getRoleName, keyword)
                    .or().like(SysRole::getRoleDesc, keyword));
        }

        wrapper.orderByDesc(SysRole::getCreatedAt);

        Page<SysRole> pageResult = roleMapper.selectPage(
                new Page<>(page.getPageNum(), page.getPageSize()), wrapper);

        return R.success(pageResult);
    }

    @Override
    public R getRoleDetail(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(404, "角色不存在");
        }

        List<SysPermission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);

        Map<String, Object> result = Map.of(
                "role", role,
                "permissions", permissions
        );

        return R.success(result);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createRole(SysRole role, List<Long> permissionIds) {
        SysRole existingRole = roleMapper.selectByRoleCode(role.getRoleCode());
        if (existingRole != null) {
            throw new BusinessException(400, "角色码已存在");
        }

        role.setStatus(1);
        role.setDeleted(0);
        roleMapper.insert(role);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(role.getId());
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }

        permissionService.clearAllPermissionCache();
        log.info("创建角色[{}], 分配权限数量[{}]", role.getRoleCode(),
                permissionIds != null ? permissionIds.size() : 0);

        return R.success("角色创建成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R updateRole(Long roleId, SysRole role, List<Long> permissionIds) {
        SysRole existingRole = roleMapper.selectById(roleId);
        if (existingRole == null || existingRole.getDeleted() == 1) {
            throw new BusinessException(404, "角色不存在");
        }

        if (!existingRole.getRoleCode().equals(role.getRoleCode())) {
            SysRole codeCheck = roleMapper.selectByRoleCode(role.getRoleCode());
            if (codeCheck != null) {
                throw new BusinessException(400, "角色码已存在");
            }
        }

        role.setId(roleId);
        roleMapper.updateById(role);

        if (permissionIds != null) {
            rolePermissionMapper.deleteByRoleId(roleId);

            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }

        permissionService.clearAllPermissionCache();
        log.info("更新角色[{}], 权限数量[{}]", roleId,
                permissionIds != null ? permissionIds.size() : 0);

        return R.success("角色更新成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteRole(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(404, "角色不存在");
        }

        if ("admin".equals(role.getRoleCode()) || "super_admin".equals(role.getRoleCode())) {
            throw new BusinessException(400, "系统内置角色不能删除");
        }

        role.setDeleted(1);
        roleMapper.updateById(role);

        rolePermissionMapper.deleteByRoleId(roleId);

        permissionService.clearAllPermissionCache();
        log.info("删除角色[{}]", roleId);

        return R.success("角色删除成功");
    }

    @Override
    public R<List<SysPermission>> getRolePermissions(Long roleId) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(404, "角色不存在");
        }

        List<SysPermission> permissions = permissionMapper.selectPermissionsByRoleId(roleId);
        return R.success(permissions);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R assignPermissions(Long roleId, List<Long> permissionIds) {
        SysRole role = roleMapper.selectById(roleId);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(404, "角色不存在");
        }

        rolePermissionMapper.deleteByRoleId(roleId);

        if (permissionIds != null && !permissionIds.isEmpty()) {
            for (Long permissionId : permissionIds) {
                SysRolePermission rolePermission = new SysRolePermission();
                rolePermission.setRoleId(roleId);
                rolePermission.setPermissionId(permissionId);
                rolePermissionMapper.insert(rolePermission);
            }
        }

        permissionService.clearAllPermissionCache();
        log.info("为角色[{}]分配权限, 数量[{}]", roleId,
                permissionIds != null ? permissionIds.size() : 0);

        return R.success("权限分配成功");
    }

    @Override
    public R<List<SysRole>> getAllRoles() {
        List<SysRole> roles = roleMapper.selectList(
                new LambdaQueryWrapper<SysRole>()
                        .eq(SysRole::getDeleted, 0)
                        .eq(SysRole::getStatus, 1)
                        .orderByAsc(SysRole::getId)
        );
        return R.success(roles);
    }
}
