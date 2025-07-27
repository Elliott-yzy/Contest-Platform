package com.w16a.danish.user.feign;

import com.w16a.danish.user.domain.dto.GithubUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Github User Client
 */
@FeignClient(
        name = "githubUserClient",
        url = "https://api.github.com",
        configuration = GithubFeignConfig.class
)
public interface GithubUserClient {

    @GetMapping("/user")
    GithubUserDTO getUserInfo(@RequestHeader("Authorization") String bearerToken);
}
