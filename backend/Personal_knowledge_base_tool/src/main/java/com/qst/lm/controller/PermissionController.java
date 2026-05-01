package com.qst.lm.controller;

import com.qst.lm.annotation.RequiresPermission;
import com.qst.lm.common.R;
import com.qst.lm.service.IPermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/permissions")
@Tag(name = "权限管理", description = "权限管理相关接口")
public class PermissionController {

    private final IPermissionService permissionService;

    public PermissionController(IPermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @GetMapping
    @Operation(summary = "权限列表", description = "获取所有权限列表")
    @RequiresPermission("user:view")
    public R getPermissionList(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取权限列表", userId);
        return permissionService.getAllPermissions();
    }

    @GetMapping("/tree")
    @Operation(summary = "权限树", description = "获取权限树结构（按模块分组）")
    @RequiresPermission("user:view")
    public R getPermissionTree(@RequestAttribute Long userId) {
        log.info("管理员[{}]获取权限树", userId);
        return permissionService.getPermissionTree();
    }

    @GetMapping("/user")
    @Operation(summary = "当前用户权限", description = "获取当前登录用户的权限码列表")
    public R getCurrentUserPermissions(@RequestAttribute Long userId) {
        log.debug("用户[{}]获取自身权限列表", userId);
        return R.success(permissionService.getUserPermissionCodes(userId));
    }
}
