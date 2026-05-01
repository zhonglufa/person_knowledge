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

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.tokenPrefix}")
    private String tokenPrefix;

    public String generateToken(String username) {
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

    public String generateToken(String username, Long userId, Long tokenVersion) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("userId", userId);
        claims.put("tokenVersion", tokenVersion == null ? 1L : tokenVersion);
        claims.put("tokenType", "user");

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

    public Claims getClaimsFromToken(String token) {
        return parseJWT(token);
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            if (expiration == null) {
                return true;
            }
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getUsernameFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }

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

    public Long getTokenVersionFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object versionObj = claims.get("tokenVersion");
            if (versionObj == null) {
                return 1L;
            }
            if (versionObj instanceof Long) {
                return (Long) versionObj;
            }
            if (versionObj instanceof Integer) {
                return ((Integer) versionObj).longValue();
            }
            return Long.parseLong(versionObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    public String getTokenTypeFromToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object tokenType = claims.get("tokenType");
            return tokenType == null ? null : tokenType.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean validateToken(String token, String username) {
        String tokenUsername = getUsernameFromToken(token);
        return (username.equals(tokenUsername) && !isTokenExpired(token));
    }

    public String extractToken(String tokenWithPrefix) {
        if (tokenWithPrefix != null && tokenWithPrefix.startsWith(tokenPrefix)) {
            return tokenWithPrefix.replace(tokenPrefix, "").trim();
        }
        return null;
    }

    public long getRemainingExpiration(String token) {
        Claims claims = getClaimsFromToken(token);
        Date expirationDate = claims.getExpiration();
        long remain = expirationDate.getTime() - System.currentTimeMillis();
        return Math.max(remain, 0);
    }

    public String generateAdminToken(Integer adminId, String username, Long tokenVersion) {
        Date nowDate = new Date();
        Date expireDate = new Date(nowDate.getTime() + expiration);

        Map<String, Object> claims = new HashMap<>();
        claims.put("adminId", adminId);
        claims.put("username", username);
        claims.put("isAdmin", true);
        claims.put("tokenVersion", tokenVersion == null ? 1L : tokenVersion);
        claims.put("tokenType", "admin");

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

    public boolean isAdminToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Object isAdmin = claims.get("isAdmin");
            return Boolean.TRUE.equals(isAdmin);
        } catch (Exception e) {
            return false;
        }
    }

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
