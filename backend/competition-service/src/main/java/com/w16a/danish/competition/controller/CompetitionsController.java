package com.w16a.danish.competition.controller;


import com.w16a.danish.competition.domain.dto.AssignJudgesDTO;
import com.w16a.danish.competition.domain.dto.CompetitionCreateDTO;
import com.w16a.danish.competition.domain.dto.CompetitionUpdateDTO;
import com.w16a.danish.competition.domain.vo.CompetitionResponseVO;
import com.w16a.danish.competition.domain.vo.PageResponse;
import com.w16a.danish.competition.domain.vo.UserBriefVO;
import com.w16a.danish.competition.service.ICompetitionsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description This class is used to handle HTTP requests related to competitions.
 */
@Tag(name = "Competition Management", description = "APIs for managing competitions")
@RestController
@RequestMapping("/competitions")
@RequiredArgsConstructor
public class CompetitionsController {

    private final ICompetitionsService competitionService;

    @Operation(
            summary = "Create a new competition",
            description = "Allows ADMIN or ORGANIZER users to create a new competition.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Competition creation request body",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CompetitionCreateDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Competition created successfully",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @PostMapping
    public ResponseEntity<CompetitionResponseVO> createCompetition(
            @RequestHeader("User-Role") String currentUserRole,
            @RequestHeader("User-ID") String currentUserId,
            @RequestBody CompetitionCreateDTO competitionDTO) {

        CompetitionResponseVO response = competitionService.createCompetition(competitionDTO, currentUserRole, currentUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get competition details",
            description = "Retrieve full competition information by ID.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Competition details retrieved successfully",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @GetMapping("/{id}")
    public ResponseEntity<CompetitionResponseVO> getCompetitionDetails(
            @Parameter(description = "Competition ID", example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f", required = true)
            @PathVariable String id) {
        CompetitionResponseVO competition = competitionService.getCompetitionById(id);
        return ResponseEntity.ok(competition);
    }

    @Operation(
            summary = "List competitions",
            description = "Returns a paginated list of competitions. Supports optional filtering by keyword, status, and category.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Competitions retrieved successfully",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/list")
    public ResponseEntity<PageResponse<CompetitionResponseVO>> listCompetitions(
            @Parameter(description = "Keyword to search by competition name", example = "AI")
            @RequestParam(required = false) String keyword,

            @Parameter(description = "Competition status filter", example = "UPCOMING")
            @RequestParam(required = false) String status,

            @Parameter(description = "Competition category filter", example = "Web")
            @RequestParam(required = false) String category,

            @Parameter(description = "Page number (starts from 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<CompetitionResponseVO> response = competitionService.listCompetitions(keyword, status, category, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a competition",
            description = "Deletes a competition by ID. Only users with ADMIN or the original ORGANIZER are authorized to perform this operation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Competition deleted successfully")
            }
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCompetition(
            @Parameter(description = "Competition ID", example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f", required = true)
            @PathVariable String id,
            @RequestHeader("User-Role") String currentUserRole,
            @RequestHeader("User-ID") String currentUserId) {

        competitionService.deleteCompetition(id, currentUserRole, currentUserId);
        return ResponseEntity.ok("Competition deleted successfully");
    }

    @Operation(
            summary = "Update competition",
            description = "Updates competition information. Only the competition creator (ORGANIZER) or an ADMIN can perform this operation.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Competition update details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CompetitionUpdateDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Competition successfully updated",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @PutMapping("/update/{id}")
    public ResponseEntity<CompetitionResponseVO> updateCompetition(
            @Parameter(description = "ID of the competition to update", example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f", required = true)
            @PathVariable String id,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestBody CompetitionUpdateDTO updateDTO) {

        CompetitionResponseVO updated = competitionService.updateCompetition(id, userId, userRole, updateDTO);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Upload competition media (intro video or images)",
            description = "Uploads a file (video or image) for a competition and updates the corresponding media URL. Only the organizer or admin can perform this operation.",
            parameters = {
                    @Parameter(name = "id", description = "Competition ID", example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f", required = true),
                    @Parameter(name = "mediaType", description = "Type of media being uploaded (VIDEO or IMAGE)", example = "VIDEO", required = true),
                    @Parameter(name = "file", description = "File to upload (image or video)", required = true,
                            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE, schema = @Schema(type = "string", format = "binary")))
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Media uploaded and competition updated",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @PostMapping("/{id}/media")
    public ResponseEntity<CompetitionResponseVO> uploadCompetitionMedia(
            @PathVariable String id,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("mediaType") String mediaType,
            @RequestParam("file") MultipartFile file) {

        CompetitionResponseVO updated = competitionService.uploadCompetitionMedia(id, userId, userRole, mediaType, file);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete an image from competition media",
            description = "Removes a specific image URL from the competition's image list. Only ADMIN or the competition organizer can perform this operation.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the competition", required = true, in = ParameterIn.PATH,
                            example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f"),
                    @Parameter(name = "imageUrl", description = "URL of the image to be deleted from the competition", required = true, in = ParameterIn.QUERY,
                            example = "https://cdn.example.com/images/banner1.jpg")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Image successfully removed and competition updated",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @DeleteMapping("/{id}/media/image")
    public ResponseEntity<CompetitionResponseVO> deleteCompetitionImage(
            @PathVariable String id,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam("imageUrl") String imageUrl) {

        CompetitionResponseVO updated = competitionService.deleteCompetitionImage(id, userId, userRole, imageUrl);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "Delete the introduction video of a competition",
            description = "Removes the intro video from the competition. Only ADMIN or the competition organizer can perform this operation.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "ID of the competition",
                            required = true,
                            in = ParameterIn.PATH,
                            example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Intro video successfully deleted and competition updated",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @DeleteMapping("/{id}/media/video")
    public ResponseEntity<CompetitionResponseVO> deleteIntroVideo(
            @PathVariable String id,
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole) {

        CompetitionResponseVO updated = competitionService.deleteIntroVideo(id, userId, userRole);
        return ResponseEntity.ok(updated);
    }

    @Operation(
            summary = "List competitions created by the current organizer",
            description = "Returns a paginated list of competitions that the logged-in organizer created.",
            parameters = {
                    @Parameter(name = "page", description = "Page number", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved competitions",
                            content = @Content(schema = @Schema(implementation = PageResponse.class))
                    )
            }
    )
    @GetMapping("/achieve/my")
    public ResponseEntity<PageResponse<CompetitionResponseVO>> listMyCompetitions(
            @RequestHeader("User-ID") String userId,
            @RequestHeader("User-Role") String userRole,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<CompetitionResponseVO> response = competitionService.listCompetitionsByOrganizer(userId, userRole, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Batch get competitions by IDs",
            description = "Returns a list of competition details given a list of competition IDs.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "List of competition IDs",
                    content = @Content(schema = @Schema(implementation = List.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved competitions")
            }
    )
    @PostMapping("/batch/ids")
    public ResponseEntity<List<CompetitionResponseVO>> getCompetitionsByIds(
            @RequestBody List<String> ids) {
        List<CompetitionResponseVO> result = competitionService.getCompetitionsByIds(ids);
        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "Assign judges to a competition",
            description = "Organizer or Admin can assign users as judges to a specific competition by providing their email addresses.",
            parameters = {
                    @Parameter(
                            name = "id",
                            description = "Competition ID to which judges are to be assigned",
                            required = true,
                            example = "86594026-4d1d-4d6d-bf8c-8950e4d1cf3f"
                    )
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Judge assignment request body containing a list of user emails",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AssignJudgesDTO.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Judges assigned successfully")
            }
    )
    @PostMapping("/{id}/assign-judges")
    public ResponseEntity<String> assignJudges(
            @RequestHeader("User-ID") String currentUserId,
            @RequestHeader("User-Role") String currentUserRole,
            @PathVariable String id,
            @RequestBody AssignJudgesDTO dto) {

        competitionService.assignJudges(id, currentUserId, currentUserRole, dto);
        return ResponseEntity.ok("Judges assigned successfully");
    }

    @Operation(
            summary = "List assigned judges for a competition",
            description = "Organizer or Admin can view all assigned judges for a specific competition.",
            parameters = {
                    @Parameter(name = "id", description = "Competition ID", required = true),
                    @Parameter(name = "page", description = "Page number (starts from 1)", example = "1"),
                    @Parameter(name = "size", description = "Page size", example = "10")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved assigned judges")
            }
    )
    @GetMapping("/{id}/judges")
    public ResponseEntity<PageResponse<UserBriefVO>> listAssignedJudges(
            @RequestHeader("User-ID") String currentUserId,
            @RequestHeader("User-Role") String currentUserRole,
            @PathVariable String id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<UserBriefVO> response = competitionService.listAssignedJudges(id, currentUserId, currentUserRole, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Remove a judge from a competition",
            description = "Organizer or Admin can remove a previously assigned judge from a competition.",
            parameters = {
                    @Parameter(name = "id", description = "Competition ID", required = true),
                    @Parameter(name = "judgeId", description = "Judge User ID to be removed", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Judge removed successfully")
            }
    )
    @DeleteMapping("/{id}/judges/{judgeId}")
    public ResponseEntity<String> removeJudge(
            @RequestHeader("User-ID") String currentUserId,
            @RequestHeader("User-Role") String currentUserRole,
            @PathVariable String id,
            @PathVariable String judgeId) {

        competitionService.removeJudge(id, currentUserId, currentUserRole, judgeId);
        return ResponseEntity.ok("Judge removed successfully.");
    }

    @Operation(
            summary = "Check if user is an organizer of a competition",
            description = "Returns true if the user is an organizer for the specified competition.",
            parameters = {
                    @Parameter(name = "competitionId", description = "Competition ID", required = true),
                    @Parameter(name = "userId", description = "User ID to check", required = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "True if the user is organizer, false otherwise")
            }
    )
    @GetMapping("/is-organizer")
    public ResponseEntity<Boolean> isUserOrganizer(
            @RequestParam("competitionId") String competitionId,
            @RequestParam("userId") String userId) {

        boolean isOrganizer = competitionService.isUserOrganizer(competitionId, userId);
        return ResponseEntity.ok(isOrganizer);
    }

    @Operation(
            summary = "Public: List all competitions (for dashboard statistics)",
            description = "Returns a full list of all competitions without pagination. Useful for platform-wide statistics, dashboard overview, etc.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved all competitions",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class))
                    )
            }
    )
    @GetMapping("/public/all")
    public ResponseEntity<List<CompetitionResponseVO>> listAllCompetitions() {
        List<CompetitionResponseVO> competitions = competitionService.listAllCompetitions();
        return ResponseEntity.ok(competitions);
    }

    @Operation(
            summary = "Update competition status (Internal use)",
            description = "Directly updates the status of a competition. No user authentication required. Intended for internal system operations.",
            parameters = {
                    @Parameter(name = "id", description = "ID of the competition", required = true),
                    @Parameter(name = "status", description = "New competition status (e.g., ONGOING, ENDED)", required = true, example = "ENDED")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Competition status updated successfully",
                            content = @Content(schema = @Schema(implementation = CompetitionResponseVO.class)))
            }
    )
    @PutMapping("/{id}/status")
    public ResponseEntity<CompetitionResponseVO> updateCompetitionStatus(
            @PathVariable("id") String competitionId,
            @RequestParam("status") String newStatus) {

        CompetitionResponseVO updated = competitionService.updateCompetitionStatus(competitionId, newStatus);
        return ResponseEntity.ok(updated);
    }

}
