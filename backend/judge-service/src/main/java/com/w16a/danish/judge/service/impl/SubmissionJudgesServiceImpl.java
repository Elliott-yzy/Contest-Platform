package com.w16a.danish.judge.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.w16a.danish.judge.domain.dto.SubmissionJudgeDTO;
import com.w16a.danish.judge.domain.enums.CompetitionStatus;
import com.w16a.danish.judge.domain.po.CompetitionJudges;
import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.w16a.danish.judge.domain.po.SubmissionJudges;
import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.vo.*;
import com.w16a.danish.judge.exception.BusinessException;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.SubmissionServiceClient;
import com.w16a.danish.judge.mapper.SubmissionJudgesMapper;
import com.w16a.danish.judge.service.ICompetitionJudgesService;
import com.w16a.danish.judge.service.ISubmissionJudgeScoresService;
import com.w16a.danish.judge.service.ISubmissionJudgesService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * Judge records for submissions
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Service
@RequiredArgsConstructor
public class SubmissionJudgesServiceImpl extends ServiceImpl<SubmissionJudgesMapper, SubmissionJudges> implements ISubmissionJudgesService {

    private final ICompetitionJudgesService competitionJudgesService;
    private final ISubmissionJudgeScoresService submissionJudgeScoresService;
    private final CompetitionServiceClient competitionServiceClient;
    private final SubmissionServiceClient submissionServiceClient;
    private final ISubmissionRecordsService submissionRecordsService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void judgeSubmission(String judgeId, SubmissionJudgeDTO judgeDTO) {
        boolean isAssignedJudge = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, judgeDTO.getCompetitionId())
                .eq(CompetitionJudges::getUserId, judgeId)
                .exists();
        if (!isAssignedJudge) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not assigned as a judge for this competition.");
        }

        boolean alreadyJudged = this.lambdaQuery()
                .eq(SubmissionJudges::getSubmissionId, judgeDTO.getSubmissionId())
                .eq(SubmissionJudges::getJudgeId, judgeId)
                .exists();
        if (alreadyJudged) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You have already judged this submission.");
        }

        CompetitionResponseVO competition = competitionServiceClient.getCompetitionById(judgeDTO.getCompetitionId()).getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        boolean isCompetitionEnded =
                (competition.getEndDate() != null && competition.getEndDate().isBefore(LocalDateTime.now())) ||
                        (CompetitionStatus.COMPLETED.equals(competition.getStatus()));
        if (!isCompetitionEnded) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition is not completed yet. Judging is not allowed.");
        }

        BigDecimal totalScore = judgeDTO.getScores().stream()
                .map(item -> item.getScore().multiply(item.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        SubmissionJudges judgeRecord = new SubmissionJudges()
                .setId(IdUtil.fastUUID())
                .setCompetitionId(judgeDTO.getCompetitionId())
                .setSubmissionId(judgeDTO.getSubmissionId())
                .setJudgeId(judgeId)
                .setTotalScore(totalScore)
                .setJudgeComments(judgeDTO.getJudgeComments());

        boolean recordSaved = this.save(judgeRecord);
        if (!recordSaved || judgeRecord.getId() == null) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save judge record.");
        }

        List<SubmissionJudgeScores> scoreList = judgeDTO.getScores().stream()
                .map(item -> new SubmissionJudgeScores()
                        .setId(IdUtil.fastUUID())
                        .setJudgeRecordId(judgeRecord.getId())
                        .setSubmissionId(judgeDTO.getSubmissionId())
                        .setCriterion(item.getCriterion())
                        .setScore(item.getScore())
                        .setWeight(item.getWeight()))
                .collect(Collectors.toList());

        boolean scoresSaved = submissionJudgeScoresService.saveBatch(scoreList);
        if (!scoresSaved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save judge score details.");
        }

        // Step 6: Update total_score and updated_at in submission_records table
        recalculateAndUpdateSubmissionTotalScore(judgeDTO.getSubmissionId());
    }

    @Override
    public boolean isUserAssignedAsJudge(String userId, String competitionId) {
        boolean assigned = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .eq(CompetitionJudges::getUserId, userId)
                .exists();
        if (!assigned) {
            return false;
        }

        CompetitionResponseVO competition = competitionServiceClient.getCompetitionById(competitionId).getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        return CompetitionStatus.COMPLETED.equals(competition.getStatus())
                || (competition.getEndDate() != null && competition.getEndDate().isBefore(LocalDateTime.now()));
    }

    @Override
    public PageResponse<SubmissionBriefVO> listPendingSubmissionsForJudging(
            String judgeId, String competitionId, String keyword, String sortOrder, int page, int size) {

        // Step 1: Verify competition status (must be completed or ended)
        CompetitionResponseVO competition = competitionServiceClient.getCompetitionById(competitionId).getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        boolean isCompleted = CompetitionStatus.COMPLETED.equals(competition.getStatus());
        boolean isEnded = competition.getEndDate() != null && competition.getEndDate().isBefore(LocalDateTime.now());

        if (!isCompleted && !isEnded) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Judging is only allowed after competition has completed or ended.");
        }

        // Step 2: Fetch all APPROVED submissions (with optional keyword and sorting)
        PageResponse<SubmissionInfoVO> approvedPage = submissionServiceClient
                .listApprovedSubmissionsPublic(competitionId, page, size, keyword, "createdAt", sortOrder)
                .getBody();

        if (approvedPage == null || approvedPage.getData() == null || approvedPage.getData().isEmpty()) {
            return PageResponse.<SubmissionBriefVO>builder()
                    .data(List.of())
                    .page(page)
                    .size(size)
                    .pages(0)
                    .total(0L)
                    .build();
        }

        List<SubmissionInfoVO> allApprovedSubmissions = approvedPage.getData();

        // Step 3: Query all submissions already judged by current judge
        List<String> judgedSubmissionIds = this.lambdaQuery()
                .eq(SubmissionJudges::getJudgeId, judgeId)
                .eq(SubmissionJudges::getCompetitionId, competitionId)
                .select(SubmissionJudges::getSubmissionId)
                .list()
                .stream()
                .map(SubmissionJudges::getSubmissionId)
                .toList();

        // Step 4: Assemble submission brief list, marking hasScored accordingly
        List<SubmissionBriefVO> resultList = allApprovedSubmissions.stream()
                .map(submission -> {
                    SubmissionBriefVO vo = new SubmissionBriefVO();
                    vo.setId(submission.getId());
                    vo.setTitle(submission.getTitle());
                    vo.setDescription(submission.getDescription());
                    vo.setFileName(submission.getFileName());
                    vo.setFileUrl(submission.getFileUrl());
                    vo.setLastUpdatedAt(submission.getCreatedAt() != null ? submission.getCreatedAt().toString() : null);
                    vo.setHasScored(judgedSubmissionIds.contains(submission.getId()));
                    return vo;
                })
                .toList();

        // Step 5: Return paginated response
        return PageResponse.<SubmissionBriefVO>builder()
                .data(resultList)
                .page(page)
                .size(size)
                .total(approvedPage.getTotal())
                .pages(approvedPage.getPages())
                .build();
    }

    @Override
    @Transactional
    public void updateJudgement(String judgeId, String submissionId, SubmissionJudgeDTO judgeDTO) {
        // Step 1: Validate existence of original judging record
        SubmissionJudges existingRecord = this.lambdaQuery()
                .eq(SubmissionJudges::getSubmissionId, submissionId)
                .eq(SubmissionJudges::getJudgeId, judgeId)
                .one();

        if (existingRecord == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No existing judging record found for this submission.");
        }

        // Step 2: Recalculate new total score
        BigDecimal newTotalScore = judgeDTO.getScores().stream()
                .map(item -> item.getScore().multiply(item.getWeight()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        // Step 3: Update judging record (comment + total score + updatedAt)
        existingRecord.setJudgeComments(judgeDTO.getJudgeComments());
        existingRecord.setTotalScore(newTotalScore);
        existingRecord.setUpdatedAt(LocalDateTime.now());
        boolean updated = this.updateById(existingRecord);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update judging record.");
        }

        // Step 4: Remove old detailed scores
        submissionJudgeScoresService.remove(
                new LambdaQueryWrapper<SubmissionJudgeScores>()
                        .eq(SubmissionJudgeScores::getJudgeRecordId, existingRecord.getId())
        );

        // Step 5: Insert new detailed scores
        List<SubmissionJudgeScores> newScoreList = judgeDTO.getScores().stream()
                .map(item -> new SubmissionJudgeScores()
                        .setId(StrUtil.uuid())
                        .setJudgeRecordId(existingRecord.getId())
                        .setSubmissionId(submissionId)
                        .setCriterion(item.getCriterion())
                        .setScore(item.getScore())
                        .setWeight(item.getWeight()))
                .collect(Collectors.toList());

        boolean scoresSaved = submissionJudgeScoresService.saveBatch(newScoreList);
        if (!scoresSaved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save updated score details.");
        }

        // Step 6: Update total_score and updated_at in submission_records table
        recalculateAndUpdateSubmissionTotalScore(judgeDTO.getSubmissionId());
    }

    @Override
    @Transactional(readOnly = true)
    public SubmissionJudgeVO getMyJudgingDetail(String judgeId, String submissionId) {
        // Step 1: Query the judge's scoring record for the submission
        SubmissionJudges judgeRecord = this.lambdaQuery()
                .eq(SubmissionJudges::getJudgeId, judgeId)
                .eq(SubmissionJudges::getSubmissionId, submissionId)
                .one();

        if (judgeRecord == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No judging record found for this submission by the current judge.");
        }

        // Step 2: Query all detailed criterion scores
        List<SubmissionJudgeScores> scoreDetails = submissionJudgeScoresService.lambdaQuery()
                .eq(SubmissionJudgeScores::getJudgeRecordId, judgeRecord.getId())
                .list();

        // Step 3: Map to VO
        SubmissionJudgeVO vo = new SubmissionJudgeVO();
        vo.setSubmissionId(judgeRecord.getSubmissionId());
        vo.setCompetitionId(judgeRecord.getCompetitionId());
        vo.setJudgeId(judgeRecord.getJudgeId());
        vo.setJudgeComments(judgeRecord.getJudgeComments());
        vo.setTotalScore(judgeRecord.getTotalScore());
        vo.setCreatedAt(judgeRecord.getCreatedAt());
        vo.setUpdatedAt(judgeRecord.getUpdatedAt());

        List<SubmissionJudgeVO.CriterionScoreVO> scoreVOList = scoreDetails.stream()
                .map(detail -> {
                    SubmissionJudgeVO.CriterionScoreVO scoreVO = new SubmissionJudgeVO.CriterionScoreVO();
                    scoreVO.setCriterion(detail.getCriterion());
                    scoreVO.setScore(detail.getScore());
                    scoreVO.setWeight(detail.getWeight());
                    return scoreVO;
                })
                .toList();

        vo.setScores(scoreVOList);

        return vo;
    }

    @Override
    public PageResponse<CompetitionResponseVO> listMyJudgingCompetitions(
            String judgeId, String keyword, String sortBy, String order, int page, int size) {

        // Step 1: Find all competitionIds where user is assigned as judge
        List<String> competitionIds = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getUserId, judgeId)
                .select(CompetitionJudges::getCompetitionId)
                .list()
                .stream()
                .map(CompetitionJudges::getCompetitionId)
                .distinct()
                .toList();

        if (competitionIds.isEmpty()) {
            return PageResponse.<CompetitionResponseVO>builder()
                    .data(List.of())
                    .page(page)
                    .size(size)
                    .pages(0)
                    .total(0L)
                    .build();
        }

        // Step 2: Fetch competition details via Feign client
        List<CompetitionResponseVO> competitions = competitionServiceClient
                .getCompetitionsByIds(competitionIds)
                .getBody();

        if (competitions == null || competitions.isEmpty()) {
            return PageResponse.<CompetitionResponseVO>builder()
                    .data(List.of())
                    .page(page)
                    .size(size)
                    .pages(0)
                    .total(0L)
                    .build();
        }

        // Step 3: Keyword filtering if necessary
        List<CompetitionResponseVO> filtered = competitions.stream()
                .filter(c -> StrUtil.isBlank(keyword) || StrUtil.containsIgnoreCase(c.getName(), keyword))
                .toList();

        // Step 4: Sorting (createdAt or endDate)
        List<CompetitionResponseVO> sorted = filtered.stream()
                .sorted((c1, c2) -> {
                    if ("endDate".equalsIgnoreCase(sortBy)) {
                        LocalDateTime e1 = c1.getEndDate();
                        LocalDateTime e2 = c2.getEndDate();
                        if (e1 == null && e2 == null) {
                            return 0;
                        }
                        if (e1 == null) {
                            return 1;
                        }
                        if (e2 == null) {
                            return -1;
                        }
                        return "asc".equalsIgnoreCase(order) ? e1.compareTo(e2) : e2.compareTo(e1);
                    } else { // default: createdAt
                        LocalDateTime c1Created = c1.getCreatedAt();
                        LocalDateTime c2Created = c2.getCreatedAt();
                        if (c1Created == null && c2Created == null) {
                            return 0;
                        }
                        if (c1Created == null) {
                            return 1;
                        }
                        if (c2Created == null) {
                            return -1;
                        }
                        return "asc".equalsIgnoreCase(order) ? c1Created.compareTo(c2Created) : c2Created.compareTo(c1Created);
                    }
                })
                .toList();

        // Step 5: Manual pagination
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, sorted.size());

        List<CompetitionResponseVO> paginated;
        if (fromIndex >= sorted.size()) {
            paginated = List.of();
        } else {
            paginated = sorted.subList(fromIndex, toIndex);
        }

        // Step 6: Return paginated response
        return PageResponse.<CompetitionResponseVO>builder()
                .data(paginated)
                .page(page)
                .size(size)
                .total((long) sorted.size())
                .pages((sorted.size() + size - 1) / size)
                .build();
    }

    private void recalculateAndUpdateSubmissionTotalScore(String submissionId) {
        List<BigDecimal> allScores = this.lambdaQuery()
                .eq(SubmissionJudges::getSubmissionId, submissionId)
                .select(SubmissionJudges::getTotalScore)
                .list()
                .stream()
                .map(SubmissionJudges::getTotalScore)
                .filter(Objects::nonNull)
                .toList();

        if (allScores.isEmpty()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "No scores found to calculate average.");
        }

        BigDecimal averageScore = allScores.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(allScores.size()), 2, RoundingMode.HALF_UP);

        boolean updatedSubmission = submissionRecordsService.lambdaUpdate()
                .eq(SubmissionRecords::getId, submissionId)
                .set(SubmissionRecords::getTotalScore, averageScore)
                .set(SubmissionRecords::getUpdatedAt, LocalDateTime.now())
                .update();

        if (!updatedSubmission) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update submission total score.");
        }
    }

}
