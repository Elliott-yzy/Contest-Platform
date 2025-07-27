package com.w16a.danish.judge.feign;

import com.w16a.danish.judge.domain.vo.CompetitionResponseVO;
import com.w16a.danish.judge.domain.vo.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * Feign client to call competition-service for fetching competition details.
 *
 * @author Eddy
 * @date 2025/04/18
 */
@FeignClient(name = "competition-service")
public interface CompetitionServiceClient {

    /**
     * Get competition details by competition ID.
     *
     * @param competitionId Competition ID
     * @return Full competition information
     */
    @GetMapping("/competitions/{id}")
    ResponseEntity<CompetitionResponseVO> getCompetitionById(@PathVariable("id") String competitionId);

    /**
     *
     * Get competition details by a list of IDs.
     *
     * @param ids list of competition IDs
     * @return {@link ResponseEntity }<{@link List }<{@link CompetitionResponseVO }>>
     */
    @PostMapping("/competitions/batch/ids")
    ResponseEntity<List<CompetitionResponseVO>> getCompetitionsByIds(@RequestBody List<String> ids);

    /**
     * Get competition details by competition ID.
     *
     * @param competitionId Competition ID
     * @return Full competition information
     */
    @GetMapping("/competitions/is-organizer")
    ResponseEntity<Boolean> isUserOrganizer(
            @RequestParam("competitionId") String competitionId,
            @RequestParam("userId") String userId);

    /**
     * List competitions created by a specific organizer (paged).
     *
     * @param userId Organizer user ID (from Request Header)
     * @param userRole Organizer role (must be ORGANIZER or ADMIN)
     * @param page Page number (default 1)
     * @param size Page size (default 10)
     * @return PageResponse containing CompetitionResponseVO
     */
    @GetMapping("/competitions/achieve/my")
    ResponseEntity<PageResponse<CompetitionResponseVO>> listMyCompetitions(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    /**
     * Fetch a full list of all competitions (public).
     * Used for platform dashboard and statistics overview.
     *
     * @return list of all public competitions
     */
    @GetMapping("/competitions/public/all")
    ResponseEntity<List<CompetitionResponseVO>> listAllCompetitions();

    /**
     * Update the status of a competition (Internal use, no auth required).
     *
     * @param competitionId Competition ID
     * @param newStatus New status to update (e.g., ONGOING, ENDED)
     * @return Updated competition information
     */
    @PutMapping("/competitions/{id}/status")
    ResponseEntity<CompetitionResponseVO> updateCompetitionStatus(
            @PathVariable("id") String competitionId,
            @RequestParam("status") String newStatus
    );

}
