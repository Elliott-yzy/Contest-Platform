package com.w16a.danish.user.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.ResponseEntity;

/**
 * Feign client for interacting with submission-service.
 * Used to query submission status related to teams.
 *
 * @author Eddy
 * @date 2025/04/18
 */
@FeignClient(name = "registration-service")
public interface SubmissionServiceClient {

    /**
     * Check if a team has any submissions.
     *
     * @param teamId ID of the team to check
     * @return ResponseEntity containing a boolean indicating if the team has submissions
     */
    @GetMapping("/submissions/internal/exists-by-team")
    ResponseEntity<Boolean> existsByTeamId(@RequestParam("teamId") String teamId);

    /**
     * Check if a team has registered for any competitions.
     *
     * @param teamId ID of the team
     * @return ResponseEntity<Boolean> whether the team has registered
     */
    @GetMapping("/registrations/internal/exists-registration-by-team")
    ResponseEntity<Boolean> existsRegistrationByTeamId(@RequestParam("teamId") String teamId);
}
