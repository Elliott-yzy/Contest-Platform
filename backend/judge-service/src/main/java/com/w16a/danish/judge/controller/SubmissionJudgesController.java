package com.w16a.danish.judge.controller;

import com.w16a.danish.judge.domain.dto.SubmissionJudgeDTO;
import com.w16a.danish.judge.domain.vo.CompetitionResponseVO;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.SubmissionBriefVO;
import com.w16a.danish.judge.domain.vo.SubmissionJudgeVO;
import com.w16a.danish.judge.service.ISubmissionJudgesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling submission judging operations.
 * Supports scoring individual and team submissions by judges.
 *
 * @author Eddy
 * @since 2025-04-18
 */
@RestController
@RequestMapping("/judges")
@RequiredArgsConstructor
@Tag(name = "Judging", description = "APIs for judges to score and comment on submissions")
public class SubmissionJudgesController {

    private final ISubmissionJudgesService submissionJudgesService;

    @Operation(
            summary = "Judge a submission",
            description = "Allows an assigned judge to score and comment a submission. Only assigned judges can perform this action.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Submission judging request body",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SubmissionJudgeDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Submission judged successfully",
                            content = @Content(schema = @Schema(example = "Submission judged successfully."))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "Forbidden - User is not assigned as a judge for this competition."
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - The judge has already scored this submission."
                    )
            }
    )
    @PostMapping("/score")
    public ResponseEntity<String> judgeSubmission(
            @RequestHeader("User-ID") String judgeId,
            @RequestBody SubmissionJudgeDTO judgeDTO) {

        submissionJudgesService.judgeSubmission(judgeId, judgeDTO);
        return ResponseEntity.ok("Submission judged successfully.");
    }

    @Operation(
            summary = "Check if current user is assigned as a judge for a competition",
            description = "Returns true if the current user is assigned as a judge for the specified competition and the competition is completed.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "competitionId", description = "Competition ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Returns true or false based on judge assignment and competition status")
            }
    )
    @GetMapping("/is-judge")
    public ResponseEntity<Boolean> isAssignedJudge(
            @RequestHeader("User-ID") String userId,
            @RequestParam("competitionId") String competitionId) {

        boolean isJudge = submissionJudgesService.isUserAssignedAsJudge(userId, competitionId);
        return ResponseEntity.ok(isJudge);
    }

    @Operation(
            summary = "Get judging detail for a submission",
            description = "Allows an assigned judge to view their detailed scoring and comments for a submission.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "submissionId", description = "Submission ID", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Judging details retrieved successfully")
            }
    )
    @GetMapping("/{submissionId}/detail")
    public ResponseEntity<SubmissionJudgeVO> getMyJudgingDetail(
            @RequestHeader("User-ID") String judgeId,
            @PathVariable("submissionId") String submissionId) {

        SubmissionJudgeVO vo = submissionJudgesService.getMyJudgingDetail(judgeId, submissionId);
        return ResponseEntity.ok(vo);
    }

    @Operation(
            summary = "List approved submissions pending judgment",
            description = "Returns a paginated list of approved submissions in the specified competition that the current judge has not yet scored. Supports keyword search and sorting.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @io.swagger.v3.oas.annotations.Parameter(name = "keyword", description = "Keyword to search submission title", required = false),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sortOrder", description = "Sort order by submission time (asc or desc)", example = "desc", required = false),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number", example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Submissions retrieved successfully")
            }
    )
    @GetMapping("/pending-submissions")
    public ResponseEntity<PageResponse<SubmissionBriefVO>> listPendingSubmissionsForJudging(
            @RequestHeader("User-ID") String judgeId,
            @RequestParam("competitionId") String competitionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<SubmissionBriefVO> response = submissionJudgesService.listPendingSubmissionsForJudging(
                judgeId, competitionId, keyword, sortOrder, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update judging for a submission",
            description = "Allows an assigned judge to update scores and comments for a submission they have already judged.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated submission judging details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SubmissionJudgeDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Judging updated successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User is not assigned as a judge for this competition or has not judged this submission yet."),
                    @ApiResponse(responseCode = "404", description = "Not Found - Submission not found for update")
            }
    )
    @PutMapping("/{submissionId}")
    public ResponseEntity<String> updateJudgement(
            @RequestHeader("User-ID") String judgeId,
            @PathVariable("submissionId") String submissionId,
            @RequestBody SubmissionJudgeDTO judgeDTO) {

        submissionJudgesService.updateJudgement(judgeId, submissionId, judgeDTO);
        return ResponseEntity.ok("Judging updated successfully.");
    }

    @Operation(
            summary = "List competitions where the user is assigned as a judge",
            description = "Returns a paginated list of competitions where the current user is assigned as a judge. Supports keyword search and sorting.",
            parameters = {
                    @io.swagger.v3.oas.annotations.Parameter(name = "keyword", description = "Keyword to search competition name", required = false),
                    @io.swagger.v3.oas.annotations.Parameter(name = "sortBy", description = "Sort by: createdAt, endDate", example = "createdAt", required = false),
                    @io.swagger.v3.oas.annotations.Parameter(name = "order", description = "Sorting order: asc or desc", example = "desc", required = false),
                    @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Page number", example = "1"),
                    @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Competitions retrieved successfully")
            }
    )
    @GetMapping("/my-competitions")
    public ResponseEntity<PageResponse<CompetitionResponseVO>> listMyJudgingCompetitions(
            @RequestHeader("User-ID") String judgeId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<CompetitionResponseVO> response = submissionJudgesService.listMyJudgingCompetitions(
                judgeId, keyword, sortBy, order, page, size);
        return ResponseEntity.ok(response);
    }

}
