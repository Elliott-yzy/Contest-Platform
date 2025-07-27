package com.w16a.danish.gateway;

import cn.hutool.jwt.JWT;
import com.w16a.danish.gateway.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtUtil class.
 * This class tests the main functionalities of parsing and verifying JWT tokens,
 * including normal, blacklisted, expired, and mismatched scenarios.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private RedisTemplate<String, String> redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private final String secret = "test-secret-key";

    @BeforeEach
    void setUp() {
        // Mock RedisTemplate and its ValueOperations
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        jwtUtil = new JwtUtil(redisTemplate);
    }

    /**
     * Generate a valid JWT token for testing with a given claims map.
     */
    private String generateToken(Map<String, Object> claims) {
        return JWT.create()
                .addPayloads(claims) // Correct usage: addPayloads not setPayloads
                .setKey(secret.getBytes(StandardCharsets.UTF_8))
                .sign();
    }

    @Test
    @DisplayName("✅ Should parse and verify a valid JWT successfully")
    void testParseAndVerifyValidToken() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "test-user");
        claims.put("role", "PARTICIPANT");
        claims.put("exp", System.currentTimeMillis() / 1000 + 3600); // expires in 1 hour
        String token = generateToken(claims);

        when(redisTemplate.hasKey("jwt:blacklist:" + token)).thenReturn(false);
        when(valueOperations.get("jwt:token:test-user")).thenReturn(token);

        // Act
        Optional<Map<String, Object>> parsedClaims = jwtUtil.parseAndVerifyToken(token, secret);

        // Assert
        assertThat(parsedClaims).isPresent();
        assertThat(parsedClaims.get().get("userId")).isEqualTo("test-user");
    }

    @Test
    @DisplayName("🛡️ Should return empty for a blacklisted token")
    void testParseAndVerifyBlacklistedToken() {
        // Arrange
        String fakeToken = "blacklisted.token.value";
        when(redisTemplate.hasKey("jwt:blacklist:" + fakeToken)).thenReturn(true);

        // Act
        Optional<Map<String, Object>> result = jwtUtil.parseAndVerifyToken(fakeToken, secret);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("🛡️ Should return empty for an expired token")
    void testParseAndVerifyExpiredToken() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "test-user");
        claims.put("role", "PARTICIPANT");
        claims.put("exp", System.currentTimeMillis() / 1000 - 10); // expired 10s ago
        String token = generateToken(claims);

        when(redisTemplate.hasKey("jwt:blacklist:" + token)).thenReturn(false);

        // Act
        Optional<Map<String, Object>> result = jwtUtil.parseAndVerifyToken(token, secret);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("🛡️ Should return empty for mismatched latest token in Redis")
    void testParseAndVerifyMismatchedToken() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "test-user");
        claims.put("role", "PARTICIPANT");
        claims.put("exp", System.currentTimeMillis() / 1000 + 3600);
        String token = generateToken(claims);

        when(redisTemplate.hasKey("jwt:blacklist:" + token)).thenReturn(false);
        when(valueOperations.get("jwt:token:test-user")).thenReturn("another-latest-token");

        // Act
        Optional<Map<String, Object>> result = jwtUtil.parseAndVerifyToken(token, secret);

        // Assert
        assertThat(result).isEmpty();
    }
}
