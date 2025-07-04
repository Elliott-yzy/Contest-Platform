package com.w16a.danish.gateway.util;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description JWT Utility with Redis integration (blacklist + latest-token check)
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * Parse and verify JWT token, with Redis blacklist and latest-token enforcement
     *
     * @param token  JWT string
     * @param secret Signing secret
     * @return Claims map if valid, otherwise empty
     */
    public Optional<Map<String, Object>> parseAndVerifyToken(String token, String secret) {
        try {
            // 1. Blacklist check
            if (Boolean.TRUE.equals(redisTemplate.hasKey("jwt:blacklist:" + token))) {
                return Optional.empty();
            }

            // 2. Signature verification
            if (!JWTUtil.verify(token, secret.getBytes(StandardCharsets.UTF_8))) {
                return Optional.empty();
            }

            // 3. Parse claims
            JWT jwt = JWTUtil.parseToken(token);
            Map<String, Object> claims = jwt.getPayloads();

            // 4. Exp check
            Number exp = (Number) claims.get("exp");
            long now = System.currentTimeMillis() / 1000;
            if (exp == null || exp.longValue() < now) {
                return Optional.empty();
            }

            // 5. Latest token check
            String userId = String.valueOf(claims.get("userId"));
            String latestToken = redisTemplate.opsForValue().get("jwt:token:" + userId);
            if (latestToken != null && !latestToken.equals(token)) {
                return Optional.empty();
            }

            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Generate JWT token based on provided claims.
     *
     * @param claims Claims to embed inside the token
     * @param secret Signing secret
     * @param expirationSeconds Expiration time in seconds
     * @return Signed JWT token string
     */
    public String generateToken(Map<String, Object> claims, String secret, long expirationSeconds) {
        long now = System.currentTimeMillis() / 1000;
        claims.put("exp", now + expirationSeconds);

        return JWT.create()
                .addPayloads(claims)
                .setKey(secret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }
}
