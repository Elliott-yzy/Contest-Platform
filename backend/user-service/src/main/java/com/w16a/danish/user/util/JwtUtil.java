package com.w16a.danish.user.util;

import cn.hutool.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description JWT creation and Redis-based session control
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Generate JWT and store it in Redis to limit one active session per user
     *
     * @param claims user info to include in token (userId, role, exp)
     * @param secret JWT signing secret
     * @param expirationMillis token expiration in milliseconds
     * @return generated JWT token
     */
    public String generateAndStoreToken(Map<String, Object> claims, String secret, long expirationMillis) {
        String token = JWTUtil.createToken(claims, secret.getBytes(StandardCharsets.UTF_8));
        String userId = String.valueOf(claims.get("userId"));
        redisTemplate.opsForValue().set("jwt:token:" + userId, token, expirationMillis, TimeUnit.MILLISECONDS);
        return token;
    }

    /**
     * Manually invalidate token by adding to blacklist
     *
     * @param token token to blacklist
     * @param expirationMillis expiration time (same as token's)
     */
    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set("jwt:blacklist:" + token, "revoked", expirationMillis, TimeUnit.MILLISECONDS);
    }
    
}
