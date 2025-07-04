package com.w16a.danish.registration.service;

import com.w16a.danish.registration.domain.dto.SubmissionReviewDTO;
import com.w16a.danish.registration.domain.po.SubmissionRecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.registration.domain.vo.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 *
 * This interface handles the submission records for competitions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/05
 */
public interface ISubmissionRecordsService extends IService<SubmissionRecords> {
    /**
     * Check if user has submitted for each competition
     * @param userId user ID
     * @param competitionIds list of competition IDs
     * @return map of competitionId -> hasSubmitted (true/false)
     */
    Map<String, Boolean> getSubmissionStatus(String userId, List<String> competitionIds);

    /**
     * Get total score of user's submissions for each competition
     * @param userId user ID
     * @param competitionIds list of competition IDs
     * @return map of competitionId -> totalScore (nullable if not scored yet)
     */
    Map<String, BigDecimal> getSubmissionScores(String userId, List<String> competitionIds);

    void deleteSubmissionsByUserAndCompetition(String userId, String competitionId);

    void submitWork(String userId, String userRole, String competitionId, String title, String description, MultipartFile file);

    SubmissionInfoVO getMySubmission(String competitionId, String userId, String userRole);

    PageResponse<SubmissionInfoVO> listSubmissionsByRole(
            String competitionId,
            String userId,
            String userRole,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order
    );

    PageResponse<SubmissionInfoVO> listPublicApprovedSubmissions(String competitionId, int page, int size, String keyword, String sortBy, String order);

    void reviewSubmission(SubmissionReviewDTO dto, String reviewerId, String reviewerRole);

    boolean isUserOrganizerOfSubmission(String submissionId, String userId);

    void deleteSubmission(String submissionId, String userId, String userRole);

    /**
     * Get submission status (whether submitted) for each team in each competition.
     */
    Map<String, Boolean> getSubmissionStatusByTeam(List<String> teamIds, List<String> competitionIds);

    /**
     * Get total score for each team in each competition (if reviewed).
     */
    Map<String, BigDecimal> getSubmissionScoresByTeam(List<String> teamIds, List<String> competitionIds);

    void submitTeamWork(
            String userId,
            String userRole,
            String competitionId,
            String teamId,
            String title,
            String description,
            MultipartFile file);

    TeamSubmissionInfoVO getTeamSubmissionPublic(String competitionId, String teamId);

    void deleteTeamSubmission(String submissionId, String userId, String userRole);

    PageResponse<SubmissionInfoVO> listTeamSubmissionsByRole(
            String competitionId, String userId, String userRole,
            int page, int size, String keyword, String sortBy, String order);

    PageResponse<SubmissionInfoVO> listPublicApprovedTeamSubmissions(
            String competitionId,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order);

    Boolean existsByTeamId(String teamId);

    SubmissionStatisticsVO getSubmissionStatistics(String competitionId);

    /**
     * Get daily submission trend for a competition.
     *
     * @param competitionId ID of the competition
     * @return Map of date -> number of submissions
     */
    Map<String, Integer> getSubmissionTrend(String competitionId);

    PlatformSubmissionStatisticsVO getPlatformSubmissionStatistics();

    /**
     * Get the platform-wide daily submission upload trend.
     * @return Map of date → number of submissions
     */
    Map<String, Integer> getPlatformSubmissionTrend();

}
