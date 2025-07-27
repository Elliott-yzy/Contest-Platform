package com.w16a.danish.registration.controller;


import com.w16a.danish.registration.domain.dto.SubmissionReviewDTO;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.service.ISubmissionRecordsService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 *
 * This class handles the submission records for competitions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/05
 */
@RestController
@RequestMapping("/submissions")
@RequiredArgsConstructor
@Tag(name = "Submission", description = "APIs for participants to submit and view their competition work")
public class SubmissionRecordsController {

    private final ISubmissionRecordsService submissionService;

    @Operation(
            summary = "Upload submission work",
            description = "Allows PARTICIPANT users to upload a file submission for a specific competition",
            parameters = {
                    @Parameter(name = "competitionId", in = ParameterIn.QUERY, required = true, description = "ID of the competition"),
                    @Parameter(name = "title", in = ParameterIn.QUERY, required = true, description = "Title of the submission"),
                    @Parameter(name = "description", in = ParameterIn.QUERY, required = true, description = "Description of the submission"),
                    @Parameter(
                            name = "file",
                            description = "File to be uploaded",
                            required = true,
                            content = @Content(
                                    mediaType = "multipart/form-data",
                                    schema = @Schema(type = "string", format = "binary")
                            )
                    )
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission uploaded successfully")
            }
    )
    @PostMapping("/upload")
    public ResponseEntity<String> uploadSubmission(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("competitionId") String competitionId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {

        submissionService.submitWork(userId, userRole, competitionId, title, description, file);
        return ResponseEntity.ok("Work submitted successfully");
    }

    @Operation(
            summary = "Delete submission",
            description = "Allows PARTICIPANT to delete their own submission. ADMIN can delete any submission.",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "Submission ID to delete")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission deleted successfully")
            }
    )
    @DeleteMapping("/{submissionId}")
    public ResponseEntity<String> deleteSubmission(
            @PathVariable String submissionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        submissionService.deleteSubmission(submissionId, userId, userRole);
        return ResponseEntity.ok("Submission deleted successfully");
    }

    @Operation(
            summary = "View submitted work",
            description = "Allows PARTICIPANT to view their submitted work for a competition",
            parameters = {
                    @Parameter(name = "competitionId", in = ParameterIn.PATH, required = true, description = "ID of the competition")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved submission info",
                            content = @Content(schema = @Schema(implementation = SubmissionInfoVO.class))
                    )
            }
    )
    @GetMapping("/{competitionId}")
    public ResponseEntity<SubmissionInfoVO> getMySubmission(
            @PathVariable String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        SubmissionInfoVO submission = submissionService.getMySubmission(competitionId, userId, userRole);
        return ResponseEntity.ok(submission);
    }

    @Operation(
            summary = "List submissions for a competition",
            description = "Returns paginated submissions. ORGANIZER and ADMIN see all",
            parameters = {
                    @Parameter(name = "competitionId", required = true, description = "ID of the competition"),
                    @Parameter(name = "page", description = "Page number (default is 1)"),
                    @Parameter(name = "size", description = "Page size (default is 10)"),
                    @Parameter(name = "keyword", description = "Search keyword (title/description)"),
                    @Parameter(name = "sortBy", description = "Sort by: title, createdAt, totalScore"),
                    @Parameter(name = "order", description = "Sorting order: asc or desc")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of submissions",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/public")
    public ResponseEntity<PageResponse<SubmissionInfoVO>> listSubmissionsForCompetition(
            @RequestParam String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        PageResponse<SubmissionInfoVO> response = submissionService.listSubmissionsByRole(
                competitionId, userId, userRole, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List approved submissions (public access)",
            description = "Anyone (including anonymous users) can view all APPROVED submissions for a competition.",
            parameters = {
                    @Parameter(name = "competitionId", required = true, description = "Competition ID"),
                    @Parameter(name = "page", description = "Page number (default is 1)"),
                    @Parameter(name = "size", description = "Page size (default is 10)"),
                    @Parameter(name = "keyword", description = "Search keyword (title/description)"),
                    @Parameter(name = "sortBy", description = "Sort by: title, createdAt, totalScore"),
                    @Parameter(name = "order", description = "Sorting order: asc or desc")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Public list of approved submissions",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/public/approved")
    public ResponseEntity<PageResponse<SubmissionInfoVO>> listApprovedSubmissionsPublic(
            @RequestParam String competitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        PageResponse<SubmissionInfoVO> response = submissionService.listPublicApprovedSubmissions(
                competitionId, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Review a submission",
            description = "Allows ORGANIZER or ADMIN to approve or reject a submission, with optional score and comments.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Review decision and comments",
                    content = @Content(schema = @Schema(implementation = SubmissionReviewDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission reviewed successfully")
            }
    )
    @PostMapping("/review")
    public ResponseEntity<String> reviewSubmission(
            @RequestHeader("User-ID") String reviewerId,
            @RequestHeader("User-Role") String reviewerRole,
            @RequestBody SubmissionReviewDTO dto) {

        submissionService.reviewSubmission(dto, reviewerId, reviewerRole);
        return ResponseEntity.ok("Submission reviewed successfully");
    }

    @Operation(
            summary = "Check if user is organizer of the competition related to a submission",
            description = "Returns true if the user is the organizer of the competition associated with the submission",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "Submission ID to check"),
                    @Parameter(name = "userId", required = true, description = "User ID to check")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "True or false depending on whether the user is an organizer"
                    )
            }
    )
    @GetMapping("/is-organizer")
    public ResponseEntity<Boolean> isUserOrganizerOfSubmission(
            @RequestParam String submissionId,
            @RequestParam String userId) {

        boolean result = submissionService.isUserOrganizerOfSubmission(submissionId, userId);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Upload submission work for a team",
            description = "Allows a team to upload a file submission for a competition",
            parameters = {
                    @Parameter(name = "competitionId", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "teamId", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "title", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "description", in = ParameterIn.QUERY, required = true),
                    @Parameter(name = "file", required = true,
                            content = @Content(mediaType = "multipart/form-data",
                                    schema = @Schema(type = "string", format = "binary")))
            }
    )
    @PostMapping("/teams/upload")
    public ResponseEntity<String> uploadTeamSubmission(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("competitionId") String competitionId,
            @RequestParam("teamId") String teamId,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("file") MultipartFile file) {

        submissionService.submitTeamWork(userId, userRole, competitionId, teamId, title, description, file);
        return ResponseEntity.ok("Team work submitted successfully");
    }

    @Operation(
            summary = "View a team's submission (Public Access)",
            description = "Anyone can view the submitted work of a team in a competition if available.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "teamId", description = "ID of the team", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team's submission retrieved successfully",
                            content = @Content(schema = @Schema(implementation = TeamSubmissionInfoVO.class))),
                    @ApiResponse(responseCode = "404", description = "Submission not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
            }
    )
    @GetMapping("/public/teams/{competitionId}/{teamId}")
    public ResponseEntity<TeamSubmissionInfoVO> getTeamSubmissionPublic(
            @PathVariable String competitionId,
            @PathVariable String teamId) {

        TeamSubmissionInfoVO submissionInfo = submissionService.getTeamSubmissionPublic(competitionId, teamId);
        return ResponseEntity.ok(submissionInfo);
    }

    @Operation(
            summary = "Delete a team's submission",
            description = "Allows a team member or ADMIN to delete a team's submission for a competition.",
            parameters = {
                    @Parameter(name = "submissionId", description = "ID of the team's submission to delete", required = true),
                    @Parameter(name = "User-ID", description = "User ID of the requester", required = true),
                    @Parameter(name = "User-Role", description = "Role of the requester (PARTICIPANT/ADMIN)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team submission deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - not authorized"),
                    @ApiResponse(responseCode = "404", description = "Submission not found")
            }
    )
    @DeleteMapping("/teams/{submissionId}")
    public ResponseEntity<String> deleteTeamSubmission(
            @PathVariable String submissionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        submissionService.deleteTeamSubmission(submissionId, userId, userRole);
        return ResponseEntity.ok("Team submission deleted successfully");
    }

    @Operation(
            summary = "List all team submissions for a competition",
            description = "Allows ORGANIZER or ADMIN to list all team submissions for a competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "keyword", description = "Optional keyword for searching title/description"),
                    @Parameter(name = "sortBy", description = "Field to sort by", example = "createdAt"),
                    @Parameter(name = "order", description = "Sorting order: asc/desc", example = "desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of team submissions", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping("/teams/list")
    public ResponseEntity<PageResponse<SubmissionInfoVO>> listTeamSubmissions(
            @RequestParam String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        PageResponse<SubmissionInfoVO> response = submissionService.listTeamSubmissionsByRole(
                competitionId, userId, userRole, page, size, keyword, sortBy, order);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Public list of approved team submissions",
            description = "Anyone can view approved team submissions for a given competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10"),
                    @Parameter(name = "keyword", description = "Optional keyword for searching title/description"),
                    @Parameter(name = "sortBy", description = "Field to sort by", example = "createdAt"),
                    @Parameter(name = "order", description = "Sorting order: asc/desc", example = "desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of approved team submissions", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping("/public/teams/approved")
    public ResponseEntity<PageResponse<SubmissionInfoVO>> listApprovedTeamSubmissionsPublic(
            @RequestParam String competitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        PageResponse<SubmissionInfoVO> response = submissionService.listPublicApprovedTeamSubmissions(
                competitionId, page, size, keyword, sortBy, order);
        return ResponseEntity.ok(response);
    }

    @Operation(hidden = true)
    @GetMapping("/internal/exists-by-team")
    public ResponseEntity<Boolean> existsByTeamId(@RequestParam String teamId) {
        boolean exists = submissionService.existsByTeamId(teamId);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Get submission statistics for a competition",
            description = "Retrieve overall submission statistics such as total submissions, approved submissions, pending submissions, and rejected submissions for a given competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission statistics retrieved successfully",
                            content = @Content(schema = @Schema(implementation = SubmissionStatisticsVO.class))),
                    @ApiResponse(responseCode = "404", description = "Competition not found")
            }
    )
    @GetMapping("/statistics")
    public ResponseEntity<SubmissionStatisticsVO> getSubmissionStatistics(
            @RequestParam String competitionId) {

        SubmissionStatisticsVO statistics = submissionService.getSubmissionStatistics(competitionId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Public: Get submission upload trend",
            description = "Retrieve the daily submission trends for a competition (date -> number of submissions). Useful for public analysis and charts.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true, example = "competition-123-uuid")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submission trends retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "404", description = "Competition not found")
            }
    )
    @GetMapping("/public/{competitionId}/submission-trend")
    public ResponseEntity<Map<String, Integer>> getSubmissionTrend(
            @PathVariable("competitionId") String competitionId) {

        Map<String, Integer> trend = submissionService.getSubmissionTrend(competitionId);
        return ResponseEntity.ok(trend);
    }

    @Operation(
            summary = "Public: Get platform submission statistics",
            description = "Retrieve platform-wide submission statistics, including total submissions, approved submissions, individual submissions, and team submissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved platform submission statistics",
                            content = @Content(schema = @Schema(implementation = PlatformSubmissionStatisticsVO.class)))
            }
    )
    @GetMapping("/public/platform/submission-statistics")
    public ResponseEntity<PlatformSubmissionStatisticsVO> getPlatformSubmissionStatistics() {
        PlatformSubmissionStatisticsVO statistics = submissionService.getPlatformSubmissionStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Public: Get platform-wide submission upload trend",
            description = "Retrieve the daily submission upload trends across the platform. Useful for platform dashboards and activity analysis.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Platform submission trends retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping("/public/platform/submission-trend")
    public ResponseEntity<Map<String, Integer>> getPlatformSubmissionTrend() {
        Map<String, Integer> trend = submissionService.getPlatformSubmissionTrend();
        return ResponseEntity.ok(trend);
    }

}
