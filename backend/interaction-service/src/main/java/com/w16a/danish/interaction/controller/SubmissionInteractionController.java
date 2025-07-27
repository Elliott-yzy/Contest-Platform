package com.w16a.danish.interaction.controller;


import com.w16a.danish.interaction.domain.dto.SubmissionCommentDTO;
import com.w16a.danish.interaction.domain.vo.InteractionStatisticsVO;
import com.w16a.danish.interaction.domain.vo.PageResponse;
import com.w16a.danish.interaction.domain.vo.SubmissionCommentVO;
import com.w16a.danish.interaction.service.ISubmissionCommentsService;
import com.w16a.danish.interaction.service.ISubmissionVotesService;
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
 * This class handles the submission interactions for competitions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
@RestController
@RequestMapping("/interactions")
@RequiredArgsConstructor
@Tag(name = "Submission Interaction", description = "APIs for commenting and voting on submissions")
public class SubmissionInteractionController {

    private final ISubmissionCommentsService commentsService;
    private final ISubmissionVotesService votesService;

    @Operation(
            summary = "Add a comment to a submission",
            description = "Allows a user to post a comment to a specific submission. Supports top-level and reply comments.",
            responses = {@ApiResponse(responseCode = "200", description = "Comment added successfully")}
    )
    @PostMapping("/comments")
    public ResponseEntity<String> postComment(@RequestBody SubmissionCommentDTO dto,
                                              @RequestHeader("User-ID") String userId) {
        commentsService.addComment(userId, dto);
        return ResponseEntity.ok("Comment added successfully");
    }

    @Operation(
            summary = "Delete a comment",
            description = "Deletes a comment. Only the comment owner, admin, or related organizer can delete.",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "ID of the comment")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Comment deleted")}
    )
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<String> deleteComment(@PathVariable String id,
                                                @RequestHeader("User-ID") String userId,
                                                @RequestHeader("User-Role") String role) {
        commentsService.deleteComment(id, userId, role);
        return ResponseEntity.ok("Comment deleted");
    }

    @Operation(
            summary = "Edit a comment",
            description = "Allows the comment owner to update the content of their comment.",
            parameters = {
                    @Parameter(name = "id", in = ParameterIn.PATH, required = true, description = "ID of the comment")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Comment updated successfully")}
    )
    @PutMapping("/comments/{id}")
    public ResponseEntity<String> updateComment(
            @PathVariable String id,
            @RequestHeader("User-ID") String userId,
            @RequestBody SubmissionCommentDTO dto) {

        commentsService.updateComment(id, userId, dto);
        return ResponseEntity.ok("Comment updated successfully");
    }

    @Operation(
            summary = "List comments for a submission",
            description = "Returns paginated comments for a submission, including nested replies.",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "Submission ID"),
                    @Parameter(name = "page", description = "Page number (default is 1)"),
                    @Parameter(name = "size", description = "Page size (default is 10)"),
                    @Parameter(name = "sortBy", description = "Sort by: createdAt or updatedAt"),
                    @Parameter(name = "order", description = "Sorting order: asc or desc")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Paginated list of comments",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/comments/list")
    public ResponseEntity<PageResponse<SubmissionCommentVO>> getComments(
            @RequestParam String submissionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        return ResponseEntity.ok(commentsService.getPaginatedComments(submissionId, page, size, sortBy, order));
    }

    @Operation(
            summary = "Vote a submission",
            description = "Allows a user to vote for a specific submission (1 user = 1 vote)",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "ID of the submission")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Voted")}
    )
    @PostMapping("/votes")
    public ResponseEntity<String> vote(@RequestParam String submissionId,
                                       @RequestHeader("User-ID") String userId) {
        votesService.vote(submissionId, userId);
        return ResponseEntity.ok("Voted");
    }

    @Operation(
            summary = "Unvote a submission",
            description = "Removes a user's vote for a specific submission",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "ID of the submission")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Unvoted")}
    )
    @DeleteMapping("/votes")
    public ResponseEntity<String> unvote(@RequestParam String submissionId,
                                         @RequestHeader("User-ID") String userId) {
        votesService.unvote(submissionId, userId);
        return ResponseEntity.ok("Unvoted");
    }

    @Operation(
            summary = "Get vote count",
            description = "Returns the number of votes a submission has received",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "ID of the submission")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Vote count returned")}
    )
    @GetMapping("/votes/count")
    public ResponseEntity<Long> getVoteCount(@RequestParam String submissionId) {
        return ResponseEntity.ok(votesService.countVotes(submissionId));
    }

    @Operation(
            summary = "Check if user has voted",
            description = "Returns whether the user has already voted for a given submission",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "ID of the submission")
            },
            responses = {@ApiResponse(responseCode = "200", description = "Vote status returned")}
    )
    @GetMapping("/votes/status")
    public ResponseEntity<Boolean> hasVoted(@RequestParam String submissionId,
                                            @RequestHeader("User-ID") String userId) {
        return ResponseEntity.ok(votesService.hasVoted(submissionId, userId));
    }

    @Operation(
            summary = "Get interaction statistics for a submission",
            description = "Returns the total number of votes and comments for a specific submission",
            parameters = {
                    @Parameter(name = "submissionId", required = true, description = "ID of the submission")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Interaction statistics retrieved successfully",
                            content = @Content(schema = @Schema(implementation = InteractionStatisticsVO.class)))
            }
    )
    @GetMapping("/statistics")
    public ResponseEntity<InteractionStatisticsVO> getInteractionStatistics(@RequestParam String submissionId) {
        InteractionStatisticsVO stats = new InteractionStatisticsVO();
        stats.setVoteCount(votesService.countVotes(submissionId));
        stats.setCommentCount(commentsService.countComments(submissionId));
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "Public: Get platform interaction statistics",
            description = "Retrieve platform-wide interaction statistics, including total votes and total comments across all submissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved platform interaction statistics",
                            content = @Content(schema = @Schema(implementation = InteractionStatisticsVO.class)))
            }
    )
    @GetMapping("/public/platform/interaction-statistics")
    public ResponseEntity<InteractionStatisticsVO> getPlatformInteractionStatistics() {
        InteractionStatisticsVO stats = new InteractionStatisticsVO();
        stats.setVoteCount(votesService.countAllVotes());
        stats.setCommentCount(commentsService.countAllComments());
        return ResponseEntity.ok(stats);
    }

}

