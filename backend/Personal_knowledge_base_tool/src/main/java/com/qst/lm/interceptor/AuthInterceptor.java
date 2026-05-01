package com.qst.lm.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.lm.common.R;
import com.qst.lm.mapper.UserMapper;
import com.qst.lm.pojo.User;
import com.qst.lm.service.IPermissionService;
import com.qst.lm.service.TokenBlacklistService;
import com.qst.lm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final IPermissionService permissionService;

    public AuthInterceptor(JwtUtils jwtUtils,
                           ObjectMapper objectMapper,
                           UserMapper userMapper,
                           TokenBlacklistService tokenBlacklistService,
                           IPermissionService permissionService) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
        this.userMapper = userMapper;
        this.tokenBlacklistService = tokenBlacklistService;
        this.permissionService = permissionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        String token = jwtUtils.extractToken(authorization);
        if (token == null || token.isBlank()) {
            writeUnauthorized(response, "未提供有效的认证信息");
            return false;
        }

        try {
            jwtUtils.parseJWT(token);

            if (jwtUtils.isTokenExpired(token)) {
                writeUnauthorized(response, "Token已过期");
                return false;
            }

            if (tokenBlacklistService.isBlacklisted(token)) {
                writeUnauthorized(response, "Token已失效");
                return false;
            }

            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                writeUnauthorized(response, "无效的Token");
                return false;
            }

            User user = userMapper.selectById(userId);
            if (user == null || user.getDeleted() == 1) {
                writeUnauthorized(response, "用户不存在");
                return false;
            }

            if (!"enabled".equals(user.getStatus())) {
                writeUnauthorized(response, "账号已被禁用");
                return false;
            }

            Long tokenVersion = jwtUtils.getTokenVersionFromToken(token);
            Long currentVersion = user.getTokenVersion() == null ? 1L : user.getTokenVersion();
            if (tokenVersion == null || !currentVersion.equals(tokenVersion)) {
                writeUnauthorized(response, "登录已失效，请重新登录");
                return false;
            }

            request.setAttribute("userId", userId);

            try {
                List<String> permissions = permissionService.getUserPermissionCodes(userId);
                request.setAttribute("userPermissions", permissions);

                List<String> roles = permissionService.getUserRoles(userId).stream()
                        .map(role -> role.getRoleCode())
                        .toList();
                request.setAttribute("userRoles", roles);
            } catch (Exception e) {
                log.warn("加载用户[{}]权限信息失败: {}", userId, e.getMessage());
            }

            return true;
        } catch (Exception e) {
            log.warn("Token验证失败：{}", e.getMessage());
            writeUnauthorized(response, "Token验证失败");
            return false;
        }
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
