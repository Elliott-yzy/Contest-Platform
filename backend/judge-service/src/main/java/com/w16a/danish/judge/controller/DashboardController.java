package com.w16a.danish.judge.controller;

import com.w16a.danish.judge.domain.vo.*;
import com.w16a.danish.judge.service.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 *
 * This controller handles dashboard-related APIs, including competition statistics,
 *
 * @author Eddy ZHANG
 * @date 2025/04/20
 */
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for competition statistics, organizer overview, and system overview")
public class DashboardController {

    private final IDashboardService dashboardService;

    @Operation(
            summary = "Public: Get competition statistics overview",
            description = "Retrieve basic statistics for a specific competition, including participant count, submission count, votes, comments, etc. "
                    + "If userId is provided, also returns the user's personal submission info.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID (UUID)", required = true, in = ParameterIn.QUERY),
                    @Parameter(name = "userId", description = "Optional: User ID (if logged in)", required = false, in = ParameterIn.QUERY)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Competition statistics retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CompetitionDashboardVO.class))),
                    @ApiResponse(responseCode = "404", description = "Competition not found")
            }
    )
    @GetMapping("/public/statistics")
    public ResponseEntity<CompetitionDashboardVO> getCompetitionStatistics(
            @RequestParam("competitionId") String competitionId,
            @RequestParam(value = "userId", required = false) String userId) {

        CompetitionDashboardVO dashboard = dashboardService.getCompetitionStatistics(competitionId, userId);
        return ResponseEntity.ok(dashboard);
    }

    @Operation(
            summary = "Public: Get platform-wide competition dashboard overview",
            description = "Retrieve overall statistics across all competitions: total competitions, total participants, total submissions, participant trends, submission trends.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Platform dashboard data retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PlatformDashboardVO.class)))
            }
    )
    @GetMapping("/public/platform-overview")
    public ResponseEntity<PlatformDashboardVO> getPlatformDashboard() {
        PlatformDashboardVO dashboard = dashboardService.getPlatformDashboard();
        return ResponseEntity.ok(dashboard);
    }

}
