package com.qst.lm.controller;

import com.qst.lm.annotation.RequiresPermission;
import com.qst.lm.common.R;
import com.qst.lm.dto.PageDTO;
import com.qst.lm.pojo.SysRole;
import com.qst.lm.service.IRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin/roles")
@Tag(name = "角色管理", description = "角色管理相关接口")
public class RoleController {

    private final IRoleService roleService;

    public RoleController(IRoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    @Operation(summary = "角色列表", description = "获取角色列表（分页）")
    @RequiresPermission("user:view")
    public R getRoleList(@RequestAttribute Long userId,
                         PageDTO page,
                         @RequestParam(required = false) String keyword) {
        log.info("管理员[{}]获取角色列表", userId);
        return roleService.getRoleList(page, keyword);
    }

    @GetMapping("/{id}")
    @Operation(summary = "角色详情", description = "获取角色详情（含权限列表）")
    @RequiresPermission("user:view")
    public R getRoleDetail(@RequestAttribute Long userId,
                           @PathVariable Long id) {
        log.info("管理员[{}]获取角色[{}]详情", userId, id);
        return roleService.getRoleDetail(id);
    }

    @PostMapping
    @Operation(summary = "创建角色", description = "创建新角色并分配权限")
    @RequiresPermission("user:create")
    public R createRole(@RequestAttribute Long userId,
                        @RequestBody Map<String, Object> params) {
        SysRole role = new SysRole();
        role.setRoleCode((String) params.get("roleCode"));
        role.setRoleName((String) params.get("roleName"));
        role.setRoleDesc((String) params.get("roleDesc"));

        @SuppressWarnings("unchecked")
        List<Long> permissionIds = ((List<?>) params.get("permissionIds")).stream()
                .map(id -> Long.valueOf(id.toString()))
                .toList();

        log.info("管理员[{}]创建角色[{}]", userId, role.getRoleCode());
        return roleService.createRole(role, permissionIds);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新角色", description = "更新角色信息和权限")
    @RequiresPermission("user:update")
    public R updateRole(@RequestAttribute Long userId,
                        @PathVariable Long id,
                        @RequestBody Map<String, Object> params) {
        SysRole role = new SysRole();
        role.setRoleCode((String) params.get("roleCode"));
        role.setRoleName((String) params.get("roleName"));
        role.setRoleDesc((String) params.get("roleDesc"));

        @SuppressWarnings("unchecked")
        List<Long> permissionIds = ((List<?>) params.get("permissionIds")).stream()
                .map(pid -> Long.valueOf(pid.toString()))
                .toList();

        log.info("管理员[{}]更新角色[{}]", userId, id);
        return roleService.updateRole(id, role, permissionIds);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除角色", description = "删除角色（逻辑删除）")
    @RequiresPermission("user:update")
    public R deleteRole(@RequestAttribute Long userId,
                        @PathVariable Long id) {
        log.info("管理员[{}]删除角色[{}]", userId, id);
        return roleService.deleteRole(id);
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "角色权限列表", description = "获取角色的权限列表")
    @RequiresPermission("user:view")
    public R getRolePermissions(@RequestAttribute Long userId,
                                @PathVariable Long id) {
        log.info("管理员[{}]获取角色[{}]权限列表", userId, id);
        return roleService.getRolePermissions(id);
    }

    @PutMapping("/{id}/permissions")
    @Operation(summary = "分配权限", description = "为角色分配权限")
    @RequiresPermission("user:update")
    public R assignPermissions(@RequestAttribute Long userId,
                               @PathVariable Long id,
                               @RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Long> permissionIds = ((List<?>) params.get("permissionIds")).stream()
                .map(pid -> Long.valueOf(pid.toString()))
                .toList();

        log.info("管理员[{}]为角色[{}]分配权限, 数量[{}]", userId, id, permissionIds.size());
        return roleService.assignPermissions(id, permissionIds);
    }

    @GetMapping("/all")
    @Operation(summary = "所有角色", description = "获取所有角色列表（不分页）")
    @RequiresPermission("user:view")
    public R getAllRoles(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取所有角色列表", userId);
        return roleService.getAllRoles();
    }
}
