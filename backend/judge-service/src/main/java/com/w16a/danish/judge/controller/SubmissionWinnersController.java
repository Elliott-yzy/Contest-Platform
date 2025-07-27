package com.w16a.danish.judge.controller;

import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.ScoredSubmissionVO;
import com.w16a.danish.judge.domain.vo.WinnerInfoVO;
import com.w16a.danish.judge.service.ISubmissionWinnersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *
 * Controller for managing competition winners.
 *
 * @author Eddy ZHANG
 * @date 2025/04/19
 */
@RestController
@RequestMapping("/winners")
@RequiredArgsConstructor
@Tag(name = "Winner Management", description = "APIs for managing and displaying competition winners")
public class SubmissionWinnersController {

    private final ISubmissionWinnersService winnersService;

    @Operation(
            summary = "Auto award winners for a competition",
            description = "Organizer or admin can trigger automatic awarding based on total scores and highest scores in specific criteria. Sends notification to participants accordingly.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true, example = "abc123-competition-id")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Auto awarding completed successfully"),
                    @ApiResponse(responseCode = "403", description = "Forbidden: Not authorized to award"),
                    @ApiResponse(responseCode = "400", description = "Bad Request: No scored submissions found")
            }
    )
    @PostMapping("/auto-award")
    public ResponseEntity<String> autoAward(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("competitionId") String competitionId) {

        winnersService.autoAward(userId, userRole, competitionId);
        return ResponseEntity.ok("Auto awarding completed successfully.");
    }

    @Operation(
            summary = "Public: List all winners for a competition",
            description = "Anyone can view all awarded submissions for a competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of winners retrieved successfully")
            }
    )
    @GetMapping("/public-list")
    public ResponseEntity<PageResponse<WinnerInfoVO>> listPublicWinners(
            @RequestParam("competitionId") String competitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<WinnerInfoVO> response = winnersService.listPublicWinners(competitionId, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List scored submissions for awarding",
            description = "Organizer or admin can view all scored submissions for a competition, sorted by total score or a specific criterion.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @Parameter(name = "keyword", description = "Optional keyword to search submissions (title/description)", required = false),
                    @Parameter(name = "sortBy", description = "Sort by totalScore or a specific criterion", required = false),
                    @Parameter(name = "order", description = "Sorting order: asc/desc", example = "desc", required = false),
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of scored submissions retrieved successfully")
            }
    )
    @GetMapping("/scored-list")
    public ResponseEntity<PageResponse<ScoredSubmissionVO>> listScoredSubmissions(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("competitionId") String competitionId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "totalScore") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String order,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ScoredSubmissionVO> response = winnersService.listScoredSubmissions(
                userId, userRole, competitionId, keyword, sortBy, order, page, size);

        return ResponseEntity.ok(response);
    }

}
