package com.w16a.danish.judge.service;

import com.w16a.danish.judge.domain.po.CompetitionJudges;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Table for competition judges (many-to-many relationship)
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
public interface ICompetitionJudgesService extends IService<CompetitionJudges> {

    /**
     * Count how many judges are assigned to a competition.
     *
     * @param competitionId Competition ID
     * @return Number of assigned judges
     */
    int countJudgesByCompetitionId(String competitionId);

}
