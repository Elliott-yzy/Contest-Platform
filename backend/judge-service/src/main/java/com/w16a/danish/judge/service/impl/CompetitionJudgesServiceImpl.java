package com.w16a.danish.judge.service.impl;

import com.w16a.danish.judge.domain.po.CompetitionJudges;
import com.w16a.danish.judge.mapper.CompetitionJudgesMapper;
import com.w16a.danish.judge.service.ICompetitionJudgesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Table for competition judges (many-to-many relationship)
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Service
@RequiredArgsConstructor
public class CompetitionJudgesServiceImpl extends ServiceImpl<CompetitionJudgesMapper, CompetitionJudges> implements ICompetitionJudgesService {

    @Override
    public int countJudgesByCompetitionId(String competitionId) {
        Long count = this.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .count();
        return Math.toIntExact(count);
    }

}
