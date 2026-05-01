package com.qst.lm.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成和验证Token
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    /**
     * 生成Token（旧版本，仅包含用户名）
     */
    public  String generateToken(String username) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 生成Token（新版本，包含用户名和用户ID）
     */
    public String generateToken(String username, Long userId) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 解析Token
     */
    public Claims parseJWT(String token) {
        try {
            SecretKey key = getSigningKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("解析JWT失败: " + e.getMessage());
        }
    }

    /**
     * 解析Token（向后兼容）
     */
    public Claims getClaimsFromToken(String token) {
        return parseJWT(token);
    }

    /**
     * 验证Token是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true; // 如果无法解析token，则认为已过期
            }
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return true; // 如果没有过期时间，则认为已过期
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object userIdObj = claims.get("userId");
            if (userIdObj != null) {
                if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else {
                    return Long.parseLong(userIdObj.toString());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证Token
     */
    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    /**
     * 从带有前缀的Token字符串中提取Token
     */
    public String extractToken(String tokenWithPrefix) {
        if (tokenWithPrefix != null && tokenWithPrefix.startsWith(tokenPrefix)) {
            return tokenWithPrefix.replace(tokenPrefix, "").trim();
        }
        return null;
    }
    
    /**
     * 生成管理员Token
     * @param adminId 管理员ID
     * @param username 管理员用户名
     * @return Token字符串
     */
    public String generateAdminToken(Integer adminId, String username) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("isAdmin", true);

        SecretKey key = getSigningKey();

        return Jwts.builder()
                .header().add("typ", "JWT").and()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(nowDate)
                .setExpiration(expireDate)
                .signWith(key, Jwts.SIG.HS512)
                .compact();
    }

    /**
     * 从Token中判断是否为管理员
     * @param token Token字符串
     * @return 是否为管理员Token
     */
    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object isAdmin = claims.get("isAdmin");
            return Boolean.TRUE.equals(isAdmin);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 从Token中获取管理员ID
     * @param token Token字符串
     * @return 管理员ID
     */
    public Integer getAdminIdFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object adminIdObj = claims.get("adminId");
            if (adminIdObj != null) {
                if (adminIdObj instanceof Integer) {
                    return (Integer) adminIdObj;
                } else {
                    return Integer.parseInt(adminIdObj.toString());
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取用于签名的密钥
     * 如果secret长度不足64字节则抛出异常
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            throw new IllegalStateException("JWT secret must be at least 64 bytes for HS512. Please configure a strong secret in application.yml");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostConstruct
    public void validateSecret() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 64) {
            throw new IllegalStateException("JWT secret must be at least 64 bytes for HS512");
        }
    }
}