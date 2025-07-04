package com.w16a.danish.competition.service;

import com.w16a.danish.competition.domain.dto.AssignJudgesDTO;
import com.w16a.danish.competition.domain.dto.CompetitionCreateDTO;
import com.w16a.danish.competition.domain.dto.CompetitionUpdateDTO;
import com.w16a.danish.competition.domain.po.Competitions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.competition.domain.vo.CompetitionResponseVO;
import com.w16a.danish.competition.domain.vo.PageResponse;
import com.w16a.danish.competition.domain.vo.UserBriefVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


/**
 * Service interface for managing competitions.
 * Provides methods for CRUD operations, media management, judge assignment, and public listings.
 *
 * @author Eddy Zhang
 * @date 2025/03/18
 */
public interface ICompetitionsService extends IService<Competitions> {

    /**
     * Create a new competition.
     *
     * @param competitionDTO Details of the competition to create.
     * @param currentUserRole Role of the user creating the competition.
     * @param currentUserId ID of the user creating the competition.
     * @return The created competition.
     */
    CompetitionResponseVO createCompetition(CompetitionCreateDTO competitionDTO, String currentUserRole, String currentUserId);

    /**
     * Delete a competition.
     *
     * @param competitionId ID of the competition to delete.
     * @param currentUserRole Role of the user requesting deletion.
     * @param currentUserId ID of the user requesting deletion.
     */
    void deleteCompetition(String competitionId, String currentUserRole, String currentUserId);

    /**
     * Get a competition by its ID.
     *
     * @param competitionId Competition ID.
     * @return Competition details.
     */
    CompetitionResponseVO getCompetitionById(String competitionId);

    /**
     * List competitions with optional filters.
     *
     * @param keyword Keyword for search.
     * @param status Competition status filter.
     * @param category Competition category filter.
     * @param page Current page number.
     * @param size Number of items per page.
     * @return Paginated list of competitions.
     */
    PageResponse<CompetitionResponseVO> listCompetitions(String keyword, String status, String category, int page, int size);

    /**
     * Update competition information.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user requesting update.
     * @param userRole Role of the user requesting update.
     * @param updateDTO Updated competition details.
     * @return Updated competition information.
     */
    CompetitionResponseVO updateCompetition(String competitionId, String userId, String userRole, CompetitionUpdateDTO updateDTO);

    /**
     * Upload competition promotional media (intro video or images).
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user uploading.
     * @param userRole Role of the user uploading.
     * @param mediaType Type of media ("VIDEO" or "IMAGE").
     * @param file Media file.
     * @return Updated competition information.
     */
    CompetitionResponseVO uploadCompetitionMedia(String competitionId, String userId, String userRole, String mediaType, MultipartFile file);

    /**
     * Delete an image from a competition.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user requesting deletion.
     * @param userRole Role of the user requesting deletion.
     * @param imageUrl URL of the image to delete.
     * @return Updated competition information.
     */
    CompetitionResponseVO deleteCompetitionImage(String competitionId, String userId, String userRole, String imageUrl);

    /**
     * List competitions created by a specific organizer.
     *
     * @param userId Organizer's user ID.
     * @param userRole Role of the user.
     * @param page Current page number.
     * @param size Number of items per page.
     * @return Paginated list of competitions.
     */
    PageResponse<CompetitionResponseVO> listCompetitionsByOrganizer(String userId, String userRole, int page, int size);

    /**
     * Delete the intro video of a competition.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user requesting deletion.
     * @param userRole Role of the user requesting deletion.
     * @return Updated competition information.
     */
    CompetitionResponseVO deleteIntroVideo(String competitionId, String userId, String userRole);

    /**
     * Get multiple competitions by their IDs.
     *
     * @param ids List of competition IDs.
     * @return List of competition details.
     */
    List<CompetitionResponseVO> getCompetitionsByIds(List<String> ids);

    /**
     * Assign judges to a competition.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user assigning judges.
     * @param userRole Role of the user.
     * @param dto Judge assignment information.
     */
    void assignJudges(String competitionId, String userId, String userRole, AssignJudgesDTO dto);

    /**
     * List judges assigned to a competition.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user requesting information.
     * @param userRole Role of the user.
     * @param page Current page number.
     * @param size Number of items per page.
     * @return Paginated list of assigned judges.
     */
    PageResponse<UserBriefVO> listAssignedJudges(String competitionId, String userId, String userRole, int page, int size);

    /**
     * Remove a judge from a competition.
     *
     * @param competitionId Competition ID.
     * @param userId ID of the user requesting removal.
     * @param userRole Role of the user.
     * @param judgeId ID of the judge to remove.
     */
    void removeJudge(String competitionId, String userId, String userRole, String judgeId);

    /**
     * Check if a user is an organizer of a competition.
     *
     * @param competitionId Competition ID.
     * @param userId User ID.
     * @return True if the user is an organizer, false otherwise.
     */
    boolean isUserOrganizer(String competitionId, String userId);

    /**
     * List all public competitions.
     *
     * @return List of public competitions.
     */
    List<CompetitionResponseVO> listAllCompetitions();

    /**
     * Update the status of a competition.
     *
     * @param competitionId Competition ID.
     * @param newStatus New status value.
     * @return Updated competition information.
     */
    CompetitionResponseVO updateCompetitionStatus(String competitionId, String newStatus);

}
