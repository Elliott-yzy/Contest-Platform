package com.w16a.danish.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Github-OAuth Client
 */
@FeignClient(
        name = "githubOAuthClient",
        url = "https://github.com",
        configuration = GithubFeignConfig.class
)
public interface GithubOAuthClient {

    @PostMapping(value = "/login/oauth/access_token", consumes = MediaType.APPLICATION_JSON_VALUE)
    Map<String, Object> getAccessToken(@RequestBody Map<String, String> request);
}
