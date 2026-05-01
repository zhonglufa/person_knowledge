package com.qst.lm.service.impl;

import com.qst.lm.service.TokenBlacklistService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "auth:blacklist:";

    private final RedisTemplate<String, Object> redisTemplate;

    public TokenBlacklistServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistToken(String token, long ttlMillis) {
        if (ttlMillis <= 0 || token == null || token.isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(buildBlacklistKey(token), "1", ttlMillis, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildBlacklistKey(token)));
    }

    private String buildBlacklistKey(String token) {
        return BLACKLIST_PREFIX + DigestUtils.md5DigestAsHex(token.getBytes(StandardCharsets.UTF_8));
    }
}
