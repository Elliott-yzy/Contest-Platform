package com.w16a.danish.gateway.filters;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.w16a.danish.gateway.config.JwtConfig;
import com.w16a.danish.gateway.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;


/**
 * @author Eddy ZHANG
 * @date 2025/03/16
 * @description JWT Authentication Filter
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtConfig jwtConfig;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getURI().getPath();

        // whitelist public URLs
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        // get JWT token from Authorization header
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StrUtil.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            return unauthorizedResponse(response, "Missing or invalid Authorization header");
        }

        // JWT token verification
        String token = authHeader.substring(7);


        Optional<Map<String, Object>> claimsOpt = jwtUtil.parseAndVerifyToken(token, jwtConfig.getSecret());

        if (claimsOpt.isEmpty()) {
            return unauthorizedResponse(response, "Invalid or expired JWT Token");
        }

        // get user information from JWT claims
        Map<String, Object> claims = claimsOpt.get();
        String userId = StrUtil.toString(claims.get("userId"));
        String userRole = StrUtil.toString(claims.get("role"));

        if (StrUtil.isBlank(userId) || StrUtil.isBlank(userRole)) {
            return unauthorizedResponse(response, "Invalid user information in JWT");
        }

        // add user information to request headers
        ServerHttpRequest modifiedRequest = request.mutate()
                .header("User-ID", userId)
                .header("User-Role", userRole)
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    /**
     * Check if the request path is in the public URL whitelist
     *
     * @param path request path
     * @return boolean
     */
    private boolean isPublicPath(String path) {
        List<String> publicUrls = jwtConfig.getPublicUrls();
        return publicUrls != null && publicUrls.stream().anyMatch(url -> pathMatcher.match(url, path));
    }

    /**
     * Return an unauthorized response with the specified message
     *
     * @param response response
     * @param message error message
     * @return {@link Mono }<{@link Void }> response
     */
    private Mono<Void> unauthorizedResponse(ServerHttpResponse response, String message) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> errorBody = new HashMap<>();
        errorBody.put("status", HttpStatus.UNAUTHORIZED.value());
        errorBody.put("error", "Unauthorized");
        errorBody.put("message", message);

        return Mono.defer(() -> response.writeWith(
                Mono.just(response.bufferFactory().wrap(JSONUtil.toJsonStr(errorBody).getBytes(StandardCharsets.UTF_8)))
        ));
    }

}
