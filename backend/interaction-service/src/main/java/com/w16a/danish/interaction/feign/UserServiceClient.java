package com.w16a.danish.interaction.feign;

import com.w16a.danish.interaction.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with user-service.
 * Used to fetch user brief info by IDs and roles.
 *
 * @author Eddy
 * @since 2025/04/04
 */
@FeignClient(name = "user-service", path = "/users")
public interface UserServiceClient {

    @PostMapping("/query-by-ids")
    ResponseEntity<List<UserBriefVO>> getUsersByIds(
            @RequestBody List<String> userIds,
            @RequestParam(required = false) String role
    );
}
