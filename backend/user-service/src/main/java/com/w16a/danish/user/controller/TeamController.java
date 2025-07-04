package com.w16a.danish.user.controller;

import com.w16a.danish.user.domain.dto.TeamCreateDTO;
import com.w16a.danish.user.domain.dto.TeamUpdateDTO;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.service.ITeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *
 * TeamController handles all team-related operations.
 *
 * @author Eddy ZHANG
 * @date 2025/04/16
 */
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "Team Management", description = "APIs for creating, managing, and querying teams")
public class TeamController {

    private final ITeamService teamService;

    @Operation(
            summary = "Create a new team",
            description = "Allows a user to create a new team. The creator will be automatically added as team leader.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Team creation request payload",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeamCreateDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Team successfully created", content = @Content(schema = @Schema(implementation = TeamResponseVO.class)))
            }
    )
    @PostMapping("/create")
    public ResponseEntity<TeamResponseVO> createTeam(
            @RequestHeader("User-ID") String creatorId,
            @Valid @RequestBody TeamCreateDTO dto
    ) {
        TeamResponseVO teamInfo = teamService.createTeam(creatorId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(teamInfo);
    }

    @Operation(
            summary = "Remove a team member (Leader only)",
            description = "Allows the team leader to remove a member from the team. Cannot remove yourself.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Member removed successfully"),
                    @ApiResponse(responseCode = "403", description = "Not authorized"),
                    @ApiResponse(responseCode = "404", description = "Team or member not found")
            }
    )
    @DeleteMapping("/{teamId}/members/{memberId}")
    public ResponseEntity<String> removeMember(
            @RequestHeader("User-ID") String requesterId,
            @PathVariable String teamId,
            @PathVariable String memberId
    ) {
        teamService.removeTeamMember(requesterId, teamId, memberId);
        return ResponseEntity.ok("Member removed");
    }

    @Operation(
            summary = "Delete a team",
            description = "Allows the team creator or an ADMIN to delete a team, only if the team has not submitted any work.",
            parameters = {
                    @Parameter(name = "User-ID", description = "User ID of the requester", required = true),
                    @Parameter(name = "User-Role", description = "Role of the requester (PARTICIPANT/ADMIN)", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team deleted successfully"),
                    @ApiResponse(responseCode = "403", description = "Unauthorized access"),
                    @ApiResponse(responseCode = "409", description = "Cannot delete team linked to submissions")
            }
    )
    @DeleteMapping("/{teamId}")
    public ResponseEntity<String> deleteTeam(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @PathVariable String teamId
    ) {
        teamService.deleteTeam(userId, userRole, teamId);
        return ResponseEntity.ok("Team deleted");
    }

    @Operation(
            summary = "Update team info",
            description = "Allows the team leader to update the team's name or description.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated team info",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TeamUpdateDTO.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team info updated successfully"),
                    @ApiResponse(responseCode = "403", description = "Not authorized to update team"),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @PutMapping("/{teamId}")
    public ResponseEntity<String> updateTeam(
            @RequestHeader("User-ID") String userId,
            @PathVariable String teamId,
            @Valid @RequestBody TeamUpdateDTO dto
    ) {
        teamService.updateTeam(userId, teamId, dto);
        return ResponseEntity.ok("Team updated");
    }

    @Operation(
            summary = "Join a team",
            description = "Allows a user to join a team if they are not already a member.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully joined the team"),
                    @ApiResponse(responseCode = "409", description = "Already a member of the team")
            }
    )
    @PostMapping("/{teamId}/join")
    public ResponseEntity<String> joinTeam(
            @RequestHeader("User-ID") String userId,
            @PathVariable String teamId
    ) {
        teamService.joinTeam(teamId, userId);
        return ResponseEntity.ok("Joined successfully");
    }

    @Operation(
            summary = "Leave a team",
            description = "Allows a user to leave a team. Team leaders cannot leave unless they delete the team or transfer leadership.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Left the team successfully"),
                    @ApiResponse(responseCode = "403", description = "Team leader cannot leave directly"),
                    @ApiResponse(responseCode = "404", description = "Team or membership not found")
            }
    )
    @PostMapping("/{teamId}/leave")
    public ResponseEntity<String> leaveTeam(
            @RequestHeader("User-ID") String userId,
            @PathVariable String teamId
    ) {
        teamService.leaveTeam(teamId, userId);
        return ResponseEntity.ok("Left the team");
    }

    @Operation(
            summary = "Get team detail by ID (public)",
            description = "Returns full information about a team including member list. This endpoint is public.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team detail returned", content = @Content(schema = @Schema(implementation = TeamResponseVO.class))),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @GetMapping("/public/{teamId}")
    public ResponseEntity<TeamResponseVO> getTeamById(@PathVariable String teamId) {
        return ResponseEntity.ok(teamService.getTeamResponseById(teamId));
    }

    @Operation(
            summary = "Get teams created by a user (public, paginated)",
            description = "Returns paginated list of teams created by a specific user. Can be filtered and sorted.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of created teams", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping("/public/created")
    public ResponseEntity<PageResponse<TeamSummaryVO>> getCreatedTeamsByUser(
            @RequestParam("userId") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(teamService.getTeamsCreatedBy(userId, page, size, sortBy, order, keyword));
    }

    @Operation(
            summary = "Get all teams I joined (paginated)",
            description = "Returns paginated list of teams that the authenticated user has joined. Includes joined time as sort field.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of joined teams", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping("/my-joined")
    public ResponseEntity<PageResponse<TeamSummaryVO>> getMyJoinedTeams(
            @RequestHeader("User-ID") String userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "joinedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(teamService.getTeamsJoinedBy(userId, page, size, sortBy, order, keyword));
    }

    @Operation(
            summary = "Get all teams (public, paginated)",
            description = "Returns a public paginated list of all teams in the platform, with optional filtering and sorting.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated list of teams", content = @Content(schema = @Schema(implementation = PageResponse.class)))
            }
    )
    @GetMapping("/public/all")
    public ResponseEntity<PageResponse<TeamSummaryVO>> getAllTeams(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(teamService.getAllTeams(page, size, sortBy, order, keyword));
    }

    @Operation(
            summary = "Get team creator info",
            description = "Returns the basic info of the team creator (used for validating team registration permissions).",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Team creator info returned"),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @GetMapping("/{teamId}/creator")
    public ResponseEntity<UserBriefVO> getTeamCreator(@PathVariable String teamId) {
        return ResponseEntity.ok(teamService.getTeamCreator(teamId));
    }

    @Operation(
            summary = "Get brief info for multiple teams",
            description = "Returns basic info (ID, name, description, createdAt) for a list of team IDs. Public endpoint.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of team brief info", content = @Content(schema = @Schema(implementation = TeamInfoVO.class)))
            }
    )
    @PostMapping("/public/brief")
    public ResponseEntity<List<TeamInfoVO>> getTeamBriefByIds(@RequestBody List<String> teamIds) {
        return ResponseEntity.ok(teamService.getTeamBriefByIds(teamIds));
    }

    @Operation(
            summary = "Check if a user is a member of a team",
            description = "Returns true if the user has joined the specified team. Useful for permission checks."
    )
    @GetMapping("/public/is-member")
    public ResponseEntity<Boolean> isUserInTeam(
            @RequestParam("userId") String userId,
            @RequestParam("teamId") String teamId
    ) {
        return ResponseEntity.ok(teamService.isUserInTeam(userId, teamId));
    }

    @Operation(
            summary = "Get all members of a team (public)",
            description = "Returns a list of all members (userId, username, email, etc.) of the specified team. Public endpoint.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of team members", content = @Content(schema = @Schema(implementation = UserBriefVO.class))),
                    @ApiResponse(responseCode = "404", description = "Team not found")
            }
    )
    @GetMapping("/public/{teamId}/members")
    public ResponseEntity<List<UserBriefVO>> getTeamMembers(@PathVariable String teamId) {
        List<UserBriefVO> members = teamService.getTeamMembers(teamId);
        return ResponseEntity.ok(members);
    }

    @Operation(
            summary = "Get all team IDs a user has joined (full list)",
            description = "Returns a full list of team IDs that the specified user has joined. Not paginated.",
            parameters = {
                    @Parameter(name = "userId", description = "User ID to query", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of joined team IDs", content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "404", description = "User not found or no joined teams")
            }
    )
    @GetMapping("/public/joined")
    public ResponseEntity<List<String>> getJoinedTeamIdsByUser(
            @RequestParam("userId") String userId
    ) {
        List<String> joinedTeamIds = teamService.getAllJoinedTeamIdsByUser(userId);
        return ResponseEntity.ok(joinedTeamIds);
    }

}
