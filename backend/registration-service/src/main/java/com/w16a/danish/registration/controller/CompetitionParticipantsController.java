package com.w16a.danish.registration.controller;


import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.service.ICompetitionParticipantsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


/**
 *
 * This class handles the registration of participants for competitions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/03
 */
@RestController
@RequestMapping("/registrations")
@RequiredArgsConstructor
@Tag(name = "Competition Registration", description = "APIs for participants to register and manage competition participation")
public class CompetitionParticipantsController {

    private final ICompetitionParticipantsService participantsService;

    @Operation(
            summary = "Register for a competition",
            description = "Allows PARTICIPANT users to register for a competition with UPCOMING or ONGOING status.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "User-ID", description = "User ID from request header", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully registered for the competition")
            }
    )
    @PostMapping("/{competitionId}")
    public ResponseEntity<String> registerForCompetition(
            @PathVariable String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        participantsService.register(competitionId, userId, userRole);
        return ResponseEntity.ok("Successfully registered for competition");
    }

    @Operation(
            summary = "Cancel registration",
            description = "Allows PARTICIPANT users to cancel their registration for a specific competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "User-ID", description = "User ID from request header", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration cancelled successfully")
            }
    )
    @DeleteMapping("/{competitionId}")
    public ResponseEntity<String> cancelRegistration(
            @PathVariable String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        participantsService.cancelRegistration(competitionId, userId, userRole);
        return ResponseEntity.ok("Registration cancelled successfully");
    }

    @Operation(
            summary = "List participants by competition",
            description = "Allows ORGANIZER to view participants of a competition with pagination, search, and sorting.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "User-ID", description = "Organizer's user ID", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true),
                    @Parameter(name = "page", description = "Page number (default is 1)"),
                    @Parameter(name = "size", description = "Page size (default is 10)"),
                    @Parameter(name = "keyword", description = "Optional search keyword (by name/email/etc)"),
                    @Parameter(name = "sortBy", description = "Sorting field (default is name)"),
                    @Parameter(name = "order", description = "Sorting order: asc or desc (default is asc)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of participants with pagination")
            }
    )
    @GetMapping("/{competitionId}/participants")
    public ResponseEntity<PageResponse<ParticipantInfoVO>> listParticipantsByCompetition(
            @PathVariable String competitionId,
            @RequestHeader("User-ID") String organizerId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        PageResponse<ParticipantInfoVO> result = participantsService.getParticipantsByCompetitionWithSearch(
                competitionId, organizerId, userRole, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Remove a participant by organizer",
            description = "Allows ORGANIZER users to forcibly cancel a participant’s registration for a competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "participantUserId", description = "User ID of the participant to remove", required = true),
                    @Parameter(name = "User-ID", description = "Organizer's user ID", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Participant registration cancelled by organizer")
            }
    )
    @DeleteMapping("/{competitionId}/participants/{participantUserId}")
    public ResponseEntity<String> cancelParticipantByOrganizer(
            @PathVariable String competitionId,
            @PathVariable String participantUserId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        participantsService.cancelByOrganizer(competitionId, participantUserId, userId, userRole);
        return ResponseEntity.ok("Participant registration cancelled by organizer");
    }

    @Operation(
            summary = "Check registration status",
            description = "Check whether the current user is already registered for the given competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "User-ID", description = "User ID from request header", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Boolean result indicating registration status")
            }
    )
    @GetMapping("/{competitionId}/status")
    public ResponseEntity<Boolean> isRegistered(
            @PathVariable String competitionId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        boolean registered = participantsService.isRegistered(competitionId, userId, userRole);
        return ResponseEntity.ok(registered);
    }

    @Operation(
            summary = "Get competitions user registered",
            description = "Allows users to retrieve paginated competitions they registered for, with optional search and sorting.",
            parameters = {
                    @Parameter(name = "User-ID", description = "User ID from request header", required = true),
                    @Parameter(name = "User-Role", description = "User role from request header", required = true),
                    @Parameter(name = "page", description = "Page number (default is 1)"),
                    @Parameter(name = "size", description = "Page size (default is 10)"),
                    @Parameter(name = "keyword", description = "Optional search keyword (by competition name)"),
                    @Parameter(name = "sortBy", description = "Sorting field (e.g., category, startDate, endDate, totalScore, joinedAt)"),
                    @Parameter(name = "order", description = "Sorting order: asc or desc (default is asc)")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of competitions the user registered")
            }
    )
    @GetMapping("/my")
    public ResponseEntity<PageResponse<CompetitionParticipationVO>> getMyCompetitions(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "competitionName") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        PageResponse<CompetitionParticipationVO> response = participantsService.getMyCompetitionsWithSearch(
                userId, userRole, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Register a team for a competition",
            description = "Allows a team to register for a competition. Only competitions with TEAM participationType are allowed.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "teamId", description = "ID of the team", required = true),
                    @Parameter(name = "User-ID", description = "User ID of the requester", required = true),
                    @Parameter(name = "User-Role", description = "User role of the requester", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team successfully registered for the competition")
            }
    )
    @PostMapping("/teams/{competitionId}/{teamId}")
    public ResponseEntity<String> registerTeam(
            @PathVariable String competitionId,
            @PathVariable String teamId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        participantsService.registerTeam(competitionId, teamId, userId, userRole);
        return ResponseEntity.ok("Team successfully registered for competition");
    }

    @Operation(
            summary = "Cancel a team's registration for a competition",
            description = "Allows a team to cancel its registration for a competition. Only applicable for competitions with TEAM participationType.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "teamId", description = "ID of the team", required = true),
                    @Parameter(name = "User-ID", description = "User ID of the requester", required = true),
                    @Parameter(name = "User-Role", description = "User role of the requester", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team registration cancelled successfully")
            }
    )
    @DeleteMapping("/teams/{competitionId}/{teamId}")
    public ResponseEntity<String> cancelTeamRegistration(
            @PathVariable String competitionId,
            @PathVariable String teamId,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        participantsService.cancelTeamRegistration(competitionId, teamId, userId, userRole);
        return ResponseEntity.ok("Team registration cancelled successfully");
    }

    @Operation(
            summary = "Check if a team is registered for a competition",
            description = "Returns true if the team has registered for the specified competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "teamId", description = "ID of the team", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "True if team is registered, false otherwise")
            }
    )
    @GetMapping("/teams/{competitionId}/{teamId}/status")
    public ResponseEntity<Boolean> isTeamRegistered(
            @PathVariable String competitionId,
            @PathVariable String teamId) {

        boolean registered = participantsService.isTeamRegistered(competitionId, teamId);
        return ResponseEntity.ok(registered);
    }

    @Operation(
            summary = "List all teams registered for a competition",
            description = "Returns a public list of teams registered for a given competition. Useful for public display or voting.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "page", description = "Page number"),
                    @Parameter(name = "size", description = "Page size"),
                    @Parameter(name = "keyword", description = "Optional keyword for searching team name"),
                    @Parameter(name = "sortBy", description = "Field to sort by", example = "createdAt"),
                    @Parameter(name = "order", description = "Sorting order: asc/desc", example = "desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of registered teams")
            }
    )
    @GetMapping("/public/{competitionId}/teams")
    public ResponseEntity<PageResponse<TeamInfoVO>> listRegisteredTeams(
            @PathVariable String competitionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order
    ) {
        PageResponse<TeamInfoVO> result = participantsService.getTeamsByCompetitionWithSearch(
                competitionId, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "List all competitions registered by a team",
            description = "Returns a paginated list of competitions that the specified team has registered for. Supports search and sorting.",
            parameters = {
                    @Parameter(name = "teamId", description = "ID of the team", required = true),
                    @Parameter(name = "page", description = "Page number (default is 1)", example = "1"),
                    @Parameter(name = "size", description = "Page size (default is 10)", example = "10"),
                    @Parameter(name = "keyword", description = "Optional keyword for searching competition name or category", example = "AI Challenge"),
                    @Parameter(name = "sortBy", description = "Field to sort by", example = "startDate"),
                    @Parameter(name = "order", description = "Sorting order: asc/desc", example = "desc")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of registered competitions")
            }
    )
    @GetMapping("/teams/{teamId}/competitions")
    public ResponseEntity<PageResponse<CompetitionParticipationVO>> getCompetitionsByTeam(
            @PathVariable String teamId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "competitionName") String sortBy,
            @RequestParam(defaultValue = "asc") String order
    ) {
        PageResponse<CompetitionParticipationVO> response = participantsService.getCompetitionsRegisteredByTeam(
                teamId, page, size, keyword, sortBy, order
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Organizer removes a registered team",
            description = "Allows ORGANIZER or ADMIN to forcibly remove a team's registration for a competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true),
                    @Parameter(name = "teamId", description = "ID of the team", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team registration removed by organizer"),
                    @ApiResponse(responseCode = "403", description = "Not authorized to perform this operation"),
                    @ApiResponse(responseCode = "404", description = "Registration record not found")
            }
    )
    @DeleteMapping("/teams/{competitionId}/team/{teamId}/by-organizer")
    public ResponseEntity<String> cancelTeamByOrganizer(
            @PathVariable String competitionId,
            @PathVariable String teamId,
            @RequestHeader("User-ID") String organizerId,
            @RequestHeader("User-Role") String userRole
    ) {
        participantsService.cancelTeamByOrganizer(competitionId, teamId, organizerId, userRole);
        return ResponseEntity.ok("Team registration removed by organizer");
    }

    @Operation(hidden = true)
    @GetMapping("/internal/exists-registration-by-team")
    public ResponseEntity<Boolean> existsRegistrationByTeamId(@RequestParam String teamId) {
        boolean exists = participantsService.existsRegistrationByTeamId(teamId);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Public: Get competition registration statistics",
            description = "Retrieve the number of individual participants, team participants, and total registrations for a specific competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true, example = "competition-123-uuid")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration statistics retrieved successfully",
                            content = @Content(schema = @Schema(implementation = RegistrationStatisticsVO.class))),
                    @ApiResponse(responseCode = "404", description = "Competition not found")
            }
    )
    @GetMapping("/public/{competitionId}/statistics")
    public ResponseEntity<RegistrationStatisticsVO> getRegistrationStatistics(
            @PathVariable("competitionId") String competitionId) {

        RegistrationStatisticsVO statistics = participantsService.getRegistrationStatistics(competitionId);
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Public: Get participant registration trend (individual & team)",
            description = "Retrieve the daily registration trends separately for individual participants and team participants of a competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "ID of the competition", required = true, example = "competition-123-uuid")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Registration trends retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Map.class))),
                    @ApiResponse(responseCode = "404", description = "Competition not found")
            }
    )
    @GetMapping("/public/{competitionId}/participant-trend")
    public ResponseEntity<Map<String, Map<String, Integer>>> getParticipantTrend(
            @PathVariable("competitionId") String competitionId) {

        Map<String, Map<String, Integer>> trend = participantsService.getParticipantTrend(competitionId);
        return ResponseEntity.ok(trend);
    }

    @Operation(
            summary = "Public: Get platform participant statistics",
            description = "Retrieve platform-wide statistics on participants, including individual and team participants.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved platform participant statistics",
                            content = @Content(schema = @Schema(implementation = PlatformParticipantStatisticsVO.class)))
            }
    )
    @GetMapping("/public/platform/participant-statistics")
    public ResponseEntity<PlatformParticipantStatisticsVO> getPlatformParticipantStatistics() {
        PlatformParticipantStatisticsVO statistics = participantsService.getPlatformParticipantStatistics();
        return ResponseEntity.ok(statistics);
    }

    @Operation(
            summary = "Public: Get platform-wide participant registration trend",
            description = "Retrieve the daily registration trends across the platform, separately for individual participants and team participants.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Platform participant trends retrieved successfully",
                            content = @Content(schema = @Schema(implementation = Map.class)))
            }
    )
    @GetMapping("/public/platform/participant-trend")
    public ResponseEntity<Map<String, Map<String, Integer>>> getPlatformParticipantTrend() {
        Map<String, Map<String, Integer>> trend = participantsService.getPlatformParticipantTrend();
        return ResponseEntity.ok(trend);
    }

}
