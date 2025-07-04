package com.w16a.danish.judge.service;

import com.w16a.danish.judge.domain.dto.SubmissionJudgeDTO;
import com.w16a.danish.judge.domain.po.SubmissionJudges;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.judge.domain.vo.CompetitionResponseVO;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.SubmissionBriefVO;
import com.w16a.danish.judge.domain.vo.SubmissionJudgeVO;

/**
 * Service interface for managing submission judging records.
 *
 * <p>
 * Handles the judging process for competition submissions,
 * including score assignment, comment recording, and tracking judge assignments.
 * </p>
 *
 * @since 2025-04-18
 */
public interface ISubmissionJudgesService extends IService<SubmissionJudges> {

    /**
     * Records a judge's evaluation for a specific submission.
     *
     * @param judgeId the ID of the judge performing the evaluation
     * @param judgeDTO the evaluation details, including score and feedback
     */
    void judgeSubmission(String judgeId, SubmissionJudgeDTO judgeDTO);

    /**
     * Checks whether a user is assigned as a judge for a given competition.
     *
     * @param userId the ID of the user
     * @param competitionId the ID of the competition
     * @return true if the user is assigned as a judge, false otherwise
     */
    boolean isUserAssignedAsJudge(String userId, String competitionId);

    /**
     * Retrieves a paginated list of submissions pending evaluation by a specific judge.
     *
     * @param judgeId the ID of the judge
     * @param competitionId the ID of the competition
     * @param keyword optional keyword for filtering by submission title or description
     * @param sortOrder sort order ("asc" or "desc") based on submission time or other criteria
     * @param page the current page number
     * @param size the number of records per page
     * @return a paginated list of pending submissions
     */
    PageResponse<SubmissionBriefVO> listPendingSubmissionsForJudging(
            String judgeId, String competitionId, String keyword, String sortOrder, int page, int size);

    /**
     * Updates an existing judging record for a specific submission.
     *
     * @param judgeId the ID of the judge
     * @param submissionId the ID of the submission being updated
     * @param judgeDTO the updated score and feedback information
     */
    void updateJudgement(String judgeId, String submissionId, SubmissionJudgeDTO judgeDTO);

    /**
     * Retrieves the judging details made by the current judge for a specific submission.
     *
     * @param judgeId the ID of the judge
     * @param submissionId the ID of the submission
     * @return a {@link SubmissionJudgeVO} containing scores and comments
     */
    SubmissionJudgeVO getMyJudgingDetail(String judgeId, String submissionId);

    /**
     * Retrieves a paginated list of competitions where the current user has judging assignments.
     *
     * @param judgeId the ID of the judge
     * @param keyword optional keyword for filtering competitions
     * @param sortBy the field to sort by (e.g., competition name, date)
     * @param order the sort order ("asc" or "desc")
     * @param page the current page number
     * @param size the number of records per page
     * @return a paginated list of competitions with assigned judging tasks
     */
    PageResponse<CompetitionResponseVO> listMyJudgingCompetitions(
            String judgeId, String keyword, String sortBy, String order, int page, int size);
}
