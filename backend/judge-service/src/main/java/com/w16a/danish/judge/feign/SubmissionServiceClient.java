package com.w16a.danish.judge.feign;

import com.w16a.danish.judge.domain.vo.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for calling Submission Service to fetch approved submissions for judging.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@FeignClient(name = "registration-service")
public interface SubmissionServiceClient {

    /**
     * List approved submissions for a given competition (public access).
     *
     * @param competitionId ID of the competition
     * @param page          Page number (default 1)
     * @param size          Page size (default 10)
     * @param keyword       Optional search keyword
     * @param sortBy        Field to sort by (default createdAt)
     * @param order         Sorting order (asc/desc)
     * @return Paginated list of approved submissions
     */
    @GetMapping("/submissions/public/approved")
    ResponseEntity<PageResponse<SubmissionInfoVO>> listApprovedSubmissionsPublic(
            @RequestParam("competitionId") String competitionId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order
    );

    /**
     * Get registration statistics (individual and team participants) for a competition.
     */
    @GetMapping("/registrations/public/{competitionId}/statistics")
    ResponseEntity<RegistrationStatisticsVO> getRegistrationStatistics(
            @PathVariable("competitionId") String competitionId
    );

    /**
     * Get submission statistics (approved, pending, rejected counts) for a competition.
     */
    @GetMapping("/submissions/statistics")
    ResponseEntity<SubmissionStatisticsVO> getSubmissionStatistics(
            @RequestParam("competitionId") String competitionId
    );

    /**
     * Get participant registration trend (individual & team) for a competition.
     */
    @GetMapping("/registrations/public/{competitionId}/participant-trend")
    ResponseEntity<Map<String, Map<String, Integer>>> getParticipantTrend(
            @PathVariable("competitionId") String competitionId
    );

    /**
     * Get submission upload trend (date -> number of submissions) for a competition.
     */
    @GetMapping("/submissions/public/{competitionId}/submission-trend")
    ResponseEntity<Map<String, Integer>> getSubmissionTrend(
            @PathVariable("competitionId") String competitionId
    );

    /**
     * Public: Get platform participant statistics (individual + team participants).
     *
     * @return PlatformParticipantStatisticsVO
     */
    @GetMapping("/registrations/public/platform/participant-statistics")
    ResponseEntity<PlatformParticipantStatisticsVO> getPlatformParticipantStatistics();

    /**
     * Public: Get platform submission statistics (approved + pending + rejected).
     *
     * @return PlatformSubmissionStatisticsVO
     */
    @GetMapping("/submissions/public/platform/submission-statistics")
    ResponseEntity<PlatformSubmissionStatisticsVO> getPlatformSubmissionStatistics();

    /**
     * Public: Get platform-wide competition dashboard overview.
     *
     * @return PlatformDashboardVO
     */
    @GetMapping("/registrations/public/platform/participant-trend")
    ResponseEntity<Map<String, Map<String, Integer>>> getPlatformParticipantTrend();

    /**
     * Public: Get platform-wide submission trend (date -> number of submissions).
     *
     * @return Map<String, Integer>
     */
    @GetMapping("/submissions/public/platform/submission-trend")
    ResponseEntity<Map<String, Integer>> getPlatformSubmissionTrend();
}
