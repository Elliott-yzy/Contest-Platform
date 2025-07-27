package com.w16a.danish.judge.service.impl;

import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.vo.SubmissionInfoVO;
import com.w16a.danish.judge.domain.vo.SubmissionScoreStatisticsVO;
import com.w16a.danish.judge.mapper.SubmissionRecordsMapper;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * <p>
 * Submission records for both individual and team competition submissions
 * </p>
 *
 * @author Eddy
 * @since 2025-04-19
 */
@Service
public class SubmissionRecordsServiceImpl extends ServiceImpl<SubmissionRecordsMapper, SubmissionRecords> implements ISubmissionRecordsService {

    @Override
    public SubmissionScoreStatisticsVO getSubmissionScoreStatistics(String competitionId) {
        List<SubmissionRecords> scoredSubmissions = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "APPROVED")
                .isNotNull(SubmissionRecords::getTotalScore)
                .list();

        if (scoredSubmissions.isEmpty()) {
            return new SubmissionScoreStatisticsVO();
        }

        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal max = null;
        BigDecimal min = null;

        for (SubmissionRecords record : scoredSubmissions) {
            BigDecimal score = record.getTotalScore();
            if (score == null) {
                continue;
            }

            sum = sum.add(score);

            if (max == null || score.compareTo(max) > 0) {
                max = score;
            }
            if (min == null || score.compareTo(min) < 0) {
                min = score;
            }
        }

        BigDecimal average = sum.divide(BigDecimal.valueOf(scoredSubmissions.size()), 2, RoundingMode.HALF_UP);

        SubmissionScoreStatisticsVO stats = new SubmissionScoreStatisticsVO();
        stats.setAverageScore(average);
        stats.setHighestScore(max);
        stats.setLowestScore(min);
        return stats;
    }

    @Override
    public SubmissionInfoVO getMySubmissionBasic(String competitionId, String userId) {
        SubmissionRecords submission = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getUserId, userId)
                .select(
                        SubmissionRecords::getId,
                        SubmissionRecords::getCompetitionId,
                        SubmissionRecords::getUserId,
                        SubmissionRecords::getTitle,
                        SubmissionRecords::getDescription,
                        SubmissionRecords::getFileName,
                        SubmissionRecords::getFileUrl,
                        SubmissionRecords::getFileType,
                        SubmissionRecords::getReviewStatus,
                        SubmissionRecords::getReviewComments,
                        SubmissionRecords::getReviewedBy,
                        SubmissionRecords::getReviewedAt,
                        SubmissionRecords::getTotalScore,
                        SubmissionRecords::getCreatedAt
                )
                .one();

        if (submission == null) {
            return null;
        }

        SubmissionInfoVO vo = new SubmissionInfoVO();
        vo.setId(submission.getId());
        vo.setCompetitionId(submission.getCompetitionId());
        vo.setUserId(submission.getUserId());
        vo.setTitle(submission.getTitle());
        vo.setDescription(submission.getDescription());
        vo.setFileName(submission.getFileName());
        vo.setFileUrl(submission.getFileUrl());
        vo.setFileType(submission.getFileType());
        vo.setReviewStatus(submission.getReviewStatus());
        vo.setReviewComments(submission.getReviewComments());
        vo.setReviewedBy(submission.getReviewedBy());
        vo.setReviewedAt(submission.getReviewedAt());
        vo.setTotalScore(submission.getTotalScore());
        vo.setCreatedAt(submission.getCreatedAt());

        return vo;
    }

    @Override
    public SubmissionInfoVO getTeamSubmissionBasic(String competitionId, String teamId) {
        SubmissionRecords submission = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getTeamId, teamId)
                .select(
                        SubmissionRecords::getId,
                        SubmissionRecords::getCompetitionId,
                        SubmissionRecords::getUserId,
                        SubmissionRecords::getTeamId,
                        SubmissionRecords::getTitle,
                        SubmissionRecords::getDescription,
                        SubmissionRecords::getFileName,
                        SubmissionRecords::getFileUrl,
                        SubmissionRecords::getFileType,
                        SubmissionRecords::getReviewStatus,
                        SubmissionRecords::getReviewComments,
                        SubmissionRecords::getReviewedBy,
                        SubmissionRecords::getReviewedAt,
                        SubmissionRecords::getTotalScore,
                        SubmissionRecords::getCreatedAt
                )
                .one();

        if (submission == null) {
            return null;
        }

        SubmissionInfoVO vo = new SubmissionInfoVO();
        vo.setId(submission.getId());
        vo.setCompetitionId(submission.getCompetitionId());
        vo.setUserId(submission.getUserId());
        vo.setTitle(submission.getTitle());
        vo.setDescription(submission.getDescription());
        vo.setFileName(submission.getFileName());
        vo.setFileUrl(submission.getFileUrl());
        vo.setFileType(submission.getFileType());
        vo.setReviewStatus(submission.getReviewStatus());
        vo.setReviewComments(submission.getReviewComments());
        vo.setReviewedBy(submission.getReviewedBy());
        vo.setReviewedAt(submission.getReviewedAt());
        vo.setTotalScore(submission.getTotalScore());
        vo.setCreatedAt(submission.getCreatedAt());
        return vo;
    }

}
