package com.qst.lm.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qst.lm.common.R;
import com.qst.lm.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 认证拦截器
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(JwtUtils jwtUtils, ObjectMapper objectMapper) {
        this.jwtUtils = jwtUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // OPTIONS请求直接放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            writeUnauthorized(response, "未提供有效的认证信息");
            return false;
        }

        String token = authorization.substring(7).trim();
        if (token.isEmpty()) {
            writeUnauthorized(response, "Token不能为空");
            return false;
        }

        try {
            if (jwtUtils.isTokenExpired(token)) {
                writeUnauthorized(response, "Token已过期");
                return false;
            }

            Long userId = jwtUtils.getUserIdFromToken(token);
            if (userId == null) {
                writeUnauthorized(response, "无效的Token");
                return false;
            }

            request.setAttribute("userId", userId);
            return true;
        } catch (Exception e) {
            log.warn("Token验证失败：{}", e.getMessage());
            writeUnauthorized(response, "Token验证失败");
            return false;
        }
    }

    /**
     * 返回401未授权响应
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        R<Void> result = R.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
