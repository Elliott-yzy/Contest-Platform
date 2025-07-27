package com.w16a.danish.user.service;

import com.w16a.danish.user.domain.dto.TeamCreateDTO;
import com.w16a.danish.user.domain.dto.TeamUpdateDTO;
import com.w16a.danish.user.domain.po.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.user.domain.vo.*;

import java.util.List;

/**
 * Service interface for managing team-related operations.
 * Supports team creation, member management, team updates, and queries.
 *
 * @author Eddy
 * @since 2025-04-16
 */
public interface ITeamService extends IService<Team> {

    /**
     * Create a new team.
     *
     * @param creatorId The ID of the user creating the team.
     * @param dto       Data transfer object containing team creation details.
     * @return A TeamResponseVO containing detailed information about the newly created team.
     */
    TeamResponseVO createTeam(String creatorId, TeamCreateDTO dto);

    /**
     * Remove a member from a team.
     *
     * @param requesterId The ID of the user requesting the removal (must be the team creator).
     * @param teamId      The ID of the team.
     * @param memberId    The ID of the member to be removed.
     */
    void removeTeamMember(String requesterId, String teamId, String memberId);

    /**
     * Delete a team.
     *
     * @param userId   The ID of the user requesting the deletion.
     * @param userRole The role of the user (must be ADMIN or the team creator).
     * @param teamId   The ID of the team to be deleted.
     */
    void deleteTeam(String userId, String userRole, String teamId);

    /**
     * Update team information.
     *
     * @param userId The ID of the user requesting the update (must be the team creator).
     * @param teamId The ID of the team to be updated.
     * @param dto    Data transfer object containing updated team details.
     */
    void updateTeam(String userId, String teamId, TeamUpdateDTO dto);

    /**
     * Join a team.
     *
     * @param teamId The ID of the team to join.
     * @param userId The ID of the user joining the team.
     */
    void joinTeam(String teamId, String userId);

    /**
     * Leave a team.
     *
     * @param teamId The ID of the team to leave.
     * @param userId The ID of the user leaving the team.
     */
    void leaveTeam(String teamId, String userId);

    /**
     * Retrieve detailed information about a specific team.
     *
     * @param teamId The ID of the team.
     * @return A TeamResponseVO containing full team details including members.
     */
    TeamResponseVO getTeamResponseById(String teamId);

    /**
     * Retrieve a paginated list of teams created by a specific user.
     *
     * @param userId  The ID of the user who created the teams.
     * @param page    The current page number.
     * @param size    The number of items per page.
     * @param sortBy  The field to sort the teams by.
     * @param order   The sort order (ascending or descending).
     * @param keyword Optional keyword for filtering teams by name or description.
     * @return A PageResponse containing a list of TeamSummaryVO objects.
     */
    PageResponse<TeamSummaryVO> getTeamsCreatedBy(String userId, int page, int size, String sortBy, String order, String keyword);

    /**
     * Retrieve a paginated list of teams that a specific user has joined.
     *
     * @param userId  The ID of the user who joined the teams.
     * @param page    The current page number.
     * @param size    The number of items per page.
     * @param sortBy  The field to sort the teams by.
     * @param order   The sort order (ascending or descending).
     * @param keyword Optional keyword for filtering teams by name or description.
     * @return A PageResponse containing a list of TeamSummaryVO objects.
     */
    PageResponse<TeamSummaryVO> getTeamsJoinedBy(String userId, int page, int size, String sortBy, String order, String keyword);

    /**
     * Retrieve a paginated list of all teams across the platform.
     *
     * @param page    The current page number.
     * @param size    The number of items per page.
     * @param sortBy  The field to sort the teams by.
     * @param order   The sort order (ascending or descending).
     * @param keyword Optional keyword for filtering teams by name or description.
     * @return A PageResponse containing a list of TeamSummaryVO objects.
     */
    PageResponse<TeamSummaryVO> getAllTeams(int page, int size, String sortBy, String order, String keyword);


    /**
     * Retrieve basic information about the creator of a team.
     *
     * @param teamId The ID of the team.
     * @return A UserBriefVO representing the team creator.
     */
    UserBriefVO getTeamCreator(String teamId);

    /**
     * Retrieve a list of brief information for multiple teams by their IDs.
     *
     * @param teamIds A list of team IDs.
     * @return A list of TeamInfoVO objects containing brief team information.
     */
    List<TeamInfoVO> getTeamBriefByIds(List<String> teamIds);

    /**
     * Check whether a user is a member of a specific team.
     *
     * @param userId The ID of the user.
     * @param teamId The ID of the team.
     * @return True if the user is a member of the team; otherwise, false.
     */
    boolean isUserInTeam(String userId, String teamId);

    /**
     * Retrieve a list of team members for a specific team.
     *
     * @param teamId The ID of the team.
     * @return A list of UserBriefVO objects representing team members.
     */
    List<UserBriefVO> getTeamMembers(String teamId);

    /**
     * Retrieve a list of all team IDs that a user has joined.
     *
     * @param userId The ID of the user.
     * @return A list of team IDs the user has joined.
     */
    List<String> getAllJoinedTeamIdsByUser(String userId);

}
