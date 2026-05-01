package com.qst.lm.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.lm.annotation.RequiresPermission;
import com.qst.lm.common.R;
import com.qst.lm.service.IPermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
@Aspect
@Component
public class PermissionAspect {

    private final IPermissionService permissionService;
    private final ObjectMapper objectMapper;

    public PermissionAspect(IPermissionService permissionService, ObjectMapper objectMapper) {
        this.permissionService = permissionService;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.qst.lm.annotation.RequiresPermission)")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        RequiresPermission requiresPermission = method.getAnnotation(RequiresPermission.class);
        if (requiresPermission == null) {
            return joinPoint.proceed();
        }

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return joinPoint.proceed();
        }

        HttpServletRequest request = attributes.getRequest();
        HttpServletResponse response = attributes.getResponse();

        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            writeForbidden(response, "未获取到用户信息");
            return null;
        }

        String permissionCode = requiresPermission.value();
        RequiresPermission.Logical logical = requiresPermission.logical();

        boolean hasPermission;
        if (logical == RequiresPermission.Logical.AND) {
            String[] codes = permissionCode.split(",");
            hasPermission = permissionService.hasAllPermissions(userId, codes);
        } else {
            String[] codes = permissionCode.split(",");
            hasPermission = permissionService.hasAnyPermission(userId, codes);
        }

        if (!hasPermission) {
            log.warn("用户[{}]权限校验失败，需要权限[{}], 请求路径[{}]",
                    userId, permissionCode, request.getRequestURI());
            writeForbidden(response, "权限不足，无法执行此操作");
            return null;
        }

        log.debug("用户[{}]权限校验通过，权限[{}]", userId, permissionCode);
        return joinPoint.proceed();
    }

    private void writeForbidden(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.error(403, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
