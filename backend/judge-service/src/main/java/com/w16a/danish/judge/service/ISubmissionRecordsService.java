package com.w16a.danish.judge.service;

import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.judge.domain.vo.SubmissionInfoVO;
import com.w16a.danish.judge.domain.vo.SubmissionScoreStatisticsVO;

/**
 * Service interface for managing submission records.
 *
 * <p>
 * Handles both individual and team submission records,
 * including retrieving submission details and calculating score statistics for competitions.
 * </p>
 *
 * @author Eddy
 * @since 2025-04-19
 */
public interface ISubmissionRecordsService extends IService<SubmissionRecords> {

    /**
     * Retrieves statistical information about submission scores for a specific competition.
     *
     * @param competitionId the ID of the competition
     * @return a {@link SubmissionScoreStatisticsVO} containing average, highest, and lowest scores
     */
    SubmissionScoreStatisticsVO getSubmissionScoreStatistics(String competitionId);

    /**
     * Retrieves the basic information of an individual participant's submission for a given competition.
     *
     * @param competitionId the ID of the competition
     * @param userId the ID of the participant
     * @return a {@link SubmissionInfoVO} containing basic submission details such as title, description, and file URL
     */
    SubmissionInfoVO getMySubmissionBasic(String competitionId, String userId);

    /**
     * Retrieves the basic information of a team's submission for a given competition.
     *
     * @param competitionId the ID of the competition
     * @param teamId the ID of the team
     * @return a {@link SubmissionInfoVO} containing basic submission details such as title, description, and file URL
     */
    SubmissionInfoVO getTeamSubmissionBasic(String competitionId, String teamId);
}
