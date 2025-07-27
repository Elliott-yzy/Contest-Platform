package com.w16a.danish.interaction.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * RegistrationServiceClient
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@FeignClient(name = "registration-service", path = "/submissions")
public interface RegistrationServiceClient {

    @GetMapping("/is-organizer")
    Boolean isUserOrganizerOfSubmission(@RequestParam("submissionId") String submissionId,
                                        @RequestParam("userId") String userId);
}
