package com.w16a.danish.judge.service.impl;

import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.w16a.danish.judge.mapper.SubmissionJudgeScoresMapper;
import com.w16a.danish.judge.service.ISubmissionJudgeScoresService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * Individual criterion scores assigned by judges
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Service
@RequiredArgsConstructor
public class SubmissionJudgeScoresServiceImpl extends ServiceImpl<SubmissionJudgeScoresMapper, SubmissionJudgeScores> implements ISubmissionJudgeScoresService {
    @Override
    public List<SubmissionJudgeScores> listBySubmissionIds(List<String> submissionIds) {
        if (submissionIds == null || submissionIds.isEmpty()) {
            return Collections.emptyList();
        }

        return this.lambdaQuery()
                .in(SubmissionJudgeScores::getSubmissionId, submissionIds)
                .list();
    }
}
