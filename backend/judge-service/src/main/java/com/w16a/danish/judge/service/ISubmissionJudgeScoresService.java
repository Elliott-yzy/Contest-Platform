package com.w16a.danish.judge.service;

import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.judge.domain.po.SubmissionRecords;

import java.util.List;

/**
 * Service interface for managing individual criterion scores assigned by judges.
 *
 * <p>
 * Handles the storage and retrieval of detailed scoring information
 * for each submission based on predefined evaluation criteria.
 * </p>
 *
 * @since 2025-04-18
 */
public interface ISubmissionJudgeScoresService extends IService<SubmissionJudgeScores> {

    /**
     * Retrieves the list of individual criterion scores for a given set of submissions.
     *
     * @param submissionIds a list of submission IDs to retrieve scores for
     * @return a list of {@link SubmissionJudgeScores} associated with the given submissions
     */
    List<SubmissionJudgeScores> listBySubmissionIds(List<String> submissionIds);
}
