package com.w16a.danish.judge.service.impl;

import cn.hutool.core.util.StrUtil;
import com.w16a.danish.judge.config.AwardNotifier;
import com.w16a.danish.judge.domain.enums.CompetitionStatus;
import com.w16a.danish.judge.domain.mq.AwardWinnerMessage;
import com.w16a.danish.judge.domain.po.SubmissionJudgeScores;
import com.w16a.danish.judge.domain.po.SubmissionJudges;
import com.w16a.danish.judge.domain.po.SubmissionRecords;
import com.w16a.danish.judge.domain.po.SubmissionWinners;
import com.w16a.danish.judge.domain.vo.*;
import com.w16a.danish.judge.exception.BusinessException;
import com.w16a.danish.judge.feign.CompetitionServiceClient;
import com.w16a.danish.judge.feign.UserServiceClient;
import com.w16a.danish.judge.mapper.SubmissionWinnersMapper;
import com.w16a.danish.judge.service.ISubmissionJudgeScoresService;
import com.w16a.danish.judge.service.ISubmissionJudgesService;
import com.w16a.danish.judge.service.ISubmissionRecordsService;
import com.w16a.danish.judge.service.ISubmissionWinnersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Table for recording awarded submissions
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
@Service
@RequiredArgsConstructor
public class SubmissionWinnersServiceImpl extends ServiceImpl<SubmissionWinnersMapper, SubmissionWinners> implements ISubmissionWinnersService {

    private final CompetitionServiceClient competitionServiceClient;
    private final ISubmissionRecordsService submissionRecordsService;
    private final ISubmissionJudgeScoresService submissionJudgeScoresService;
    private final ISubmissionJudgesService submissionJudgesService;
    private final UserServiceClient userServiceClient;
    private final AwardNotifier awardNotifier;

    @Override
    public PageResponse<ScoredSubmissionVO> listScoredSubmissions(
            String userId,
            String userRole,
            String competitionId,
            String keyword,
            String sortBy,
            String order,
            int page,
            int size) {

        boolean isOrganizerOrAdmin = "ADMIN".equalsIgnoreCase(userRole) ||
                Boolean.TRUE.equals(competitionServiceClient.isUserOrganizer(competitionId, userId).getBody());
        if (!isOrganizerOrAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers or admins can view scored submissions.");
        }

        List<SubmissionRecords> submissions = submissionRecordsService.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .isNotNull(SubmissionRecords::getTotalScore)
                .list();

        if (submissions.isEmpty()) {
            return emptyPageResponse(page, size);
        }

        List<String> submissionIds = submissions.stream()
                .map(SubmissionRecords::getId)
                .toList();

        Map<String, Map<String, BigDecimal>> submissionCriteriaScoreMap = submissionJudgeScoresService
                .listBySubmissionIds(submissionIds)
                .stream()
                .collect(Collectors.groupingBy(
                        SubmissionJudgeScores::getSubmissionId,
                        Collectors.toMap(
                                SubmissionJudgeScores::getCriterion,
                                SubmissionJudgeScores::getScore,
                                (existing, replacement) -> existing
                        )
                ));

        Map<String, Long> submissionJudgeCountMap = submissionJudgesService.lambdaQuery()
                .in(SubmissionJudges::getSubmissionId, submissionIds)
                .select(SubmissionJudges::getSubmissionId)
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        SubmissionJudges::getSubmissionId,
                        Collectors.counting()
                ));

        List<ScoredSubmissionVO> voList = submissions.stream()
                .filter(submission -> submissionJudgeCountMap.getOrDefault(submission.getId(), 0L) >= 3)
                .map(submission -> {
                    ScoredSubmissionVO vo = new ScoredSubmissionVO();
                    vo.setSubmissionId(submission.getId());
                    vo.setTitle(submission.getTitle());
                    vo.setTotalScore(Optional.ofNullable(submission.getTotalScore()).orElse(BigDecimal.ZERO));
                    vo.setIsWinner(false);
                    vo.setCriterionScores(submissionCriteriaScoreMap.getOrDefault(submission.getId(), Map.of()));
                    return vo;
                })
                .toList();

        if (StrUtil.isNotBlank(keyword)) {
            voList = voList.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getTitle(), keyword))
                    .toList();
        }

        Comparator<ScoredSubmissionVO> comparator;
        if (StrUtil.isNotBlank(sortBy) && !"totalScore".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(
                    (ScoredSubmissionVO vo) -> Optional.ofNullable(vo.getCriterionScores().get(sortBy)).orElse(BigDecimal.ZERO),
                    "desc".equalsIgnoreCase(order) ? Comparator.reverseOrder() : Comparator.naturalOrder()
            );
        } else {
            comparator = Comparator.comparing(
                    (ScoredSubmissionVO vo) -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO),
                    "desc".equalsIgnoreCase(order) ? Comparator.reverseOrder() : Comparator.naturalOrder()
            );
        }
        comparator = comparator.thenComparing(ScoredSubmissionVO::getSubmissionId);

        voList = voList.stream().sorted(comparator).toList();

        int total = voList.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ScoredSubmissionVO> pagedList = fromIndex >= total ? List.of() : voList.subList(fromIndex, toIndex);

        return PageResponse.<ScoredSubmissionVO>builder()
                .data(pagedList)
                .page(page)
                .size(size)
                .pages((total + size - 1) / size)
                .total((long) total)
                .build();
    }

    @Override
    @Transactional
    public void autoAward(String userId, String userRole, String competitionId) {
        boolean isOrganizerOrAdmin = "ADMIN".equalsIgnoreCase(userRole) ||
                Boolean.TRUE.equals(competitionServiceClient.isUserOrganizer(competitionId, userId).getBody());
        if (!isOrganizerOrAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers or admins can auto-award submissions.");
        }

        List<SubmissionRecords> submissions = submissionRecordsService.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .isNotNull(SubmissionRecords::getTotalScore)
                .list();
        if (submissions.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No scored submissions found for this competition.");
        }

        List<String> submissionIds = submissions.stream()
                .map(SubmissionRecords::getId)
                .toList();

        Map<String, Map<String, BigDecimal>> submissionCriteriaScoreMap =
                submissionJudgeScoresService.listBySubmissionIds(submissionIds)
                        .stream()
                        .collect(Collectors.groupingBy(
                                SubmissionJudgeScores::getSubmissionId,
                                Collectors.toMap(
                                        SubmissionJudgeScores::getCriterion,
                                        SubmissionJudgeScores::getScore,
                                        (existing, replacement) -> existing
                                )
                        ));

        List<SubmissionRecords> sortedSubmissions = submissions.stream()
                .sorted(Comparator
                        .comparing((SubmissionRecords s) -> Optional.ofNullable(s.getTotalScore()).orElse(BigDecimal.ZERO))
                        .reversed()
                        .thenComparing(SubmissionRecords::getId)
                )
                .toList();

        List<SubmissionWinners> winners = new ArrayList<>();
        BigDecimal prevScore = null;
        int currentRank = 1;
        int assignedRanks = 0;
        int maxRanks = 3;

        for (SubmissionRecords submission : sortedSubmissions) {
            BigDecimal score = Optional.ofNullable(submission.getTotalScore()).orElse(BigDecimal.ZERO);

            if (prevScore == null || score.compareTo(prevScore) < 0) {
                currentRank = assignedRanks + 1;
            }

            if (currentRank > maxRanks) {
                break;
            }

            String awardName = switch (currentRank) {
                case 1 -> "Champion";
                case 2 -> "Runner-up";
                case 3 -> "Second Runner-up";
                default -> "Awarded";
            };

            winners.add(buildWinner(competitionId, submission.getId(), awardName, currentRank));

            prevScore = score;
            assignedRanks++;
        }

        Map<String, BigDecimal> bestScoresPerCriterion = new HashMap<>();
        Map<String, List<String>> bestSubmissionIdsPerCriterion = new HashMap<>();

        for (Map.Entry<String, Map<String, BigDecimal>> entry : submissionCriteriaScoreMap.entrySet()) {
            String submissionId = entry.getKey();
            Map<String, BigDecimal> criteriaScores = entry.getValue();

            for (Map.Entry<String, BigDecimal> criterionScore : criteriaScores.entrySet()) {
                String criterion = criterionScore.getKey();
                BigDecimal score = criterionScore.getValue();

                BigDecimal bestScore = bestScoresPerCriterion.get(criterion);
                if (bestScore == null || score.compareTo(bestScore) > 0) {
                    bestScoresPerCriterion.put(criterion, score);
                    bestSubmissionIdsPerCriterion.put(criterion, new ArrayList<>(List.of(submissionId)));
                } else if (score.compareTo(bestScore) == 0) {
                    bestSubmissionIdsPerCriterion.get(criterion).add(submissionId);
                }
            }
        }

        for (Map.Entry<String, List<String>> entry : bestSubmissionIdsPerCriterion.entrySet()) {
            String criterion = entry.getKey();
            List<String> bestSubmissionIds = entry.getValue();

            for (String submissionId : bestSubmissionIds) {
                winners.add(buildWinner(competitionId, submissionId, "Best in " + criterion, null));
            }
        }

        this.lambdaUpdate()
                .eq(SubmissionWinners::getCompetitionId, competitionId)
                .remove();
        this.saveBatch(winners);

        competitionServiceClient.updateCompetitionStatus(competitionId, CompetitionStatus.AWARDED.name());

        submissions.forEach(submission -> {
            boolean hasAnyAward = winners.stream()
                    .anyMatch(w -> w.getSubmissionId().equals(submission.getId()));

            if (hasAnyAward) {
                sendAwardNotification(submission, competitionId, winners);
            }
        });
    }

    @Override
    public PageResponse<WinnerInfoVO> listPublicWinners(String competitionId, int page, int size) {
        List<SubmissionWinners> winners = this.lambdaQuery()
                .eq(SubmissionWinners::getCompetitionId, competitionId)
                .list();

        if (winners.isEmpty()) {
            return new PageResponse<>(List.of(), 0, page, size, 0);
        }

        Map<String, List<SubmissionWinners>> winnersGroupedBySubmission = winners.stream()
                .collect(Collectors.groupingBy(SubmissionWinners::getSubmissionId));

        List<String> submissionIds = winnersGroupedBySubmission.keySet().stream().toList();

        List<SubmissionRecords> submissions = submissionRecordsService.lambdaQuery()
                .in(SubmissionRecords::getId, submissionIds)
                .list();

        if (submissions.isEmpty()) {
            return new PageResponse<>(List.of(), 0, page, size, 0);
        }

        Set<String> userIds = new HashSet<>();
        Set<String> teamIds = new HashSet<>();

        for (SubmissionRecords submission : submissions) {
            if (StrUtil.isNotBlank(submission.getUserId())) {
                userIds.add(submission.getUserId());
            }
            if (StrUtil.isNotBlank(submission.getTeamId())) {
                teamIds.add(submission.getTeamId());
            }
        }

        Map<String, String> userNameMap = Optional.ofNullable(userServiceClient.getUsersByIds(userIds.stream().toList(), null).getBody())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(UserBriefVO::getId, UserBriefVO::getName));

        Map<String, String> teamNameMap = Optional.ofNullable(userServiceClient.getTeamBriefByIds(teamIds.stream().toList()).getBody())
                .orElse(List.of())
                .stream()
                .collect(Collectors.toMap(TeamInfoVO::getTeamId, TeamInfoVO::getTeamName));

        List<WinnerInfoVO> voList = submissions.stream()
                .map(submission -> {
                    WinnerInfoVO vo = new WinnerInfoVO();
                    vo.setSubmissionId(submission.getId());
                    vo.setTitle(submission.getTitle());
                    vo.setAwards(winnersGroupedBySubmission.getOrDefault(submission.getId(), List.of())
                            .stream()
                            .map(SubmissionWinners::getAwardName)
                            .distinct()
                            .toList());
                    vo.setTotalScore(submission.getTotalScore());
                    vo.setIsTeamSubmission(StrUtil.isNotBlank(submission.getTeamId()));
                    vo.setSubmittedAt(submission.getCreatedAt());

                    if (vo.getIsTeamSubmission()) {
                        vo.setSubmitterName(teamNameMap.getOrDefault(submission.getTeamId(), "Unknown Team"));
                    } else {
                        vo.setSubmitterName(userNameMap.getOrDefault(submission.getUserId(), "Unknown User"));
                    }

                    return vo;
                })
                .sorted(Comparator.comparing(WinnerInfoVO::getTotalScore, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .toList();

        int total = voList.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<WinnerInfoVO> pagedList = fromIndex >= total ? List.of() : voList.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    private SubmissionWinners buildWinner(String competitionId, String submissionId, String awardName, Integer rank) {
        return new SubmissionWinners()
                .setId(StrUtil.uuid())
                .setCompetitionId(competitionId)
                .setSubmissionId(submissionId)
                .setAwardName(awardName)
                .setRankSubmission(rank)
                .setAwardDescription(null);
    }

    private void sendAwardNotification(SubmissionRecords submission, String competitionId, List<SubmissionWinners> winners) {
        CompetitionResponseVO competition = competitionServiceClient.getCompetitionById(competitionId).getBody();
        if (competition == null) {
            return;
        }

        List<UserBriefVO> recipients = new ArrayList<>();

        if (StrUtil.isNotBlank(submission.getTeamId())) {
            recipients = Optional.ofNullable(userServiceClient.getTeamMembersByTeamId(submission.getTeamId()).getBody())
                    .orElse(Collections.emptyList());
        } else if (StrUtil.isNotBlank(submission.getUserId())) {
            UserBriefVO user = userServiceClient.getUserBriefById(submission.getUserId()).getBody();
            if (user != null) {
                recipients = List.of(user);
            }
        }

        if (recipients.isEmpty()) {
            return;
        }

        List<String> awardNames = winners.stream()
                .filter(w -> w.getSubmissionId().equals(submission.getId()))
                .map(SubmissionWinners::getAwardName)
                .toList();

        if (awardNames.isEmpty()) {
            return;
        }

        String awards = String.join(", ", awardNames);

        for (UserBriefVO recipient : recipients) {
            AwardWinnerMessage message = new AwardWinnerMessage();
            message.setUserName(recipient.getName());
            message.setUserEmail(recipient.getEmail());
            message.setCompetitionName(competition.getName());
            message.setAwardedAt(LocalDateTime.now());
            message.setAwardName(awards);

            awardNotifier.sendAwardWinner(message);
        }
    }

    private AwardWinnerMessage buildAwardMessage(String userName, String userEmail, SubmissionRecords submission, CompetitionResponseVO competition, boolean isWinner, List<SubmissionWinners> winners) {
        AwardWinnerMessage message = new AwardWinnerMessage();
        message.setUserName(userName);
        message.setUserEmail(userEmail);
        message.setCompetitionName(competition.getName());
        message.setAwardedAt(LocalDateTime.now());

        if (isWinner) {
            winners.stream()
                    .filter(w -> w.getSubmissionId().equals(submission.getId()))
                    .findFirst()
                    .ifPresent(w -> message.setAwardName(w.getAwardName()));
        } else {
            message.setAwardName("None");
        }

        return message;
    }

    /**
     * Helper method to create an empty PageResponse.
     */
    private PageResponse<ScoredSubmissionVO> emptyPageResponse(int page, int size) {
        return PageResponse.<ScoredSubmissionVO>builder()
                .data(List.of())
                .page(page)
                .size(size)
                .pages(0)
                .total(0L)
                .build();
    }

}
