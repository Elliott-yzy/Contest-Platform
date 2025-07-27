package com.w16a.danish.judge.feign;

import com.w16a.danish.judge.domain.vo.InteractionStatisticsVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Feign client for calling Interaction Service to fetch interaction statistics (votes and comments).
 *
 * @author Eddy
 * @date 2025/04/21
 */
@FeignClient(name = "interaction-service")
public interface InteractionServiceClient {

    /**
     * Get vote and comment statistics for a specific submission.
     *
     * @param submissionId ID of the submission
     * @return InteractionStatisticsVO containing vote count and comment count
     */
    @GetMapping("/interactions/statistics")
    ResponseEntity<InteractionStatisticsVO> getInteractionStatistics(
            @RequestParam("submissionId") String submissionId
    );

    /**
     * Get platform-wide interaction statistics (total votes and comments).
     *
     * @return InteractionStatisticsVO containing total vote count and total comment count
     */
    @GetMapping("/interactions/public/platform/interaction-statistics")
    ResponseEntity<InteractionStatisticsVO> getPlatformInteractionStatistics();
}
