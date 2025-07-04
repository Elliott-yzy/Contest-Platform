package com.w16a.danish.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author Eddy ZHANG
 * @date 2025/03/26
 * @description Google OAuth Client
 */
@FeignClient(
        name = "googleOAuthClient",
        url = "https://oauth2.googleapis.com",
        configuration = GoogleFeignConfig.class
)
public interface GoogleOAuthClient {

    @PostMapping(value = "/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Map<String, Object> getAccessToken(@RequestBody MultiValueMap<String, String> request);
}
