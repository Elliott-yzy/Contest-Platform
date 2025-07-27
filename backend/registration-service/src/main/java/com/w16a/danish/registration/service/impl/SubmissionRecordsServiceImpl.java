package com.w16a.danish.registration.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.w16a.danish.registration.config.SubmissionNotifier;
import com.w16a.danish.registration.domain.dto.SubmissionReviewDTO;
import com.w16a.danish.registration.domain.mq.SubmissionReviewedMessage;
import com.w16a.danish.registration.domain.mq.SubmissionUploadedMessage;
import com.w16a.danish.registration.domain.po.CompetitionOrganizers;
import com.w16a.danish.registration.domain.po.CompetitionParticipants;
import com.w16a.danish.registration.domain.po.SubmissionRecords;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.enums.CompetitionStatus;
import com.w16a.danish.registration.exception.BusinessException;
import com.w16a.danish.registration.feign.CompetitionServiceClient;
import com.w16a.danish.registration.feign.FileServiceClient;
import com.w16a.danish.registration.feign.UserServiceClient;
import com.w16a.danish.registration.mapper.SubmissionRecordsMapper;
import com.w16a.danish.registration.service.ICompetitionOrganizersService;
import com.w16a.danish.registration.service.ICompetitionParticipantsService;
import com.w16a.danish.registration.service.ISubmissionRecordsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * This class handles the management of submission records.
 *
 * @author Eddy ZHANG
 * @date 2025/04/03
 */
@Service
@RequiredArgsConstructor
public class SubmissionRecordsServiceImpl extends ServiceImpl<SubmissionRecordsMapper, SubmissionRecords> implements ISubmissionRecordsService {

    private final CompetitionServiceClient competitionServiceClient;
    private final FileServiceClient fileServiceClient;
    private final SubmissionNotifier submissionNotifier;
    private final UserServiceClient userServiceClient;

    @Lazy
    @Autowired
    private ICompetitionParticipantsService competitionParticipantsService;

    @Lazy
    @Autowired
    private ICompetitionOrganizersService competitionOrganizersService;

    @Override
    public Map<String, Boolean> getSubmissionStatus(String userId, List<String> competitionIds) {
        if (competitionIds == null || competitionIds.isEmpty()) {
            return Map.of();
        }

        List<SubmissionRecords> records = this.lambdaQuery()
                .eq(SubmissionRecords::getUserId, userId)
                .in(SubmissionRecords::getCompetitionId, competitionIds)
                .list();

        return records.stream()
                .collect(Collectors.toMap(
                        SubmissionRecords::getCompetitionId,
                        r -> true,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public Map<String, BigDecimal> getSubmissionScores(String userId, List<String> competitionIds) {
        if (competitionIds == null || competitionIds.isEmpty()) {
            return Map.of();
        }

        List<SubmissionRecords> records = this.lambdaQuery()
                .eq(SubmissionRecords::getUserId, userId)
                .in(SubmissionRecords::getCompetitionId, competitionIds)
                .list();

        return records.stream()
                .filter(r -> r.getTotalScore() != null)
                .collect(Collectors.toMap(
                        SubmissionRecords::getCompetitionId,
                        SubmissionRecords::getTotalScore,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    @Transactional
    public void deleteSubmissionsByUserAndCompetition(String userId, String competitionId) {
        SubmissionRecords submission = lambdaQuery()
                .eq(SubmissionRecords::getUserId, userId)
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .one();

        if (submission != null) {
            deleteFileByUrl(submission.getFileUrl());
            this.removeById(submission.getId());
        }
    }

    @Override
    @Transactional
    public void submitWork(String userId, String userRole, String competitionId, String title, String description, MultipartFile file) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can submit work");
        }

        boolean registered = competitionParticipantsService.lambdaQuery()
                .eq(CompetitionParticipants::getUserId, userId)
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .exists();
        if (!registered) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You must register before submitting work");
        }

        CompetitionResponseVO competition;
        try {
            ResponseEntity<CompetitionResponseVO> response = competitionServiceClient.getCompetitionById(competitionId);
            competition = response.getBody();

            if (competition == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
            }

            if (!CompetitionStatus.isRegistrable(competition.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot submit work to this competition");
            }

            if (competition.getEndDate() != null && competition.getEndDate().isBefore(LocalDateTime.now())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "The competition has already ended");
            }
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to verify competition: " + e.getMessage());
        }

        String uploadedUrl = Optional.ofNullable(fileServiceClient.uploadSubmission(file).getBody())
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed"));

        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();

        SubmissionRecords existing = lambdaQuery()
                .eq(SubmissionRecords::getUserId, userId)
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .one();

        if (existing != null) {
            deleteFileByUrl(existing.getFileUrl());
            existing.setTitle(title);
            existing.setDescription(description);
            existing.setFileName(fileName);
            existing.setFileUrl(uploadedUrl);
            existing.setFileType(fileType);
            existing.setReviewStatus("PENDING");
            existing.setReviewedBy(null);
            existing.setReviewedAt(null);
            existing.setReviewComments(null);
            existing.setTotalScore(null);

            boolean updated = this.updateById(existing);
            if (!updated) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update submission");
            }
        } else {
            SubmissionRecords submission = new SubmissionRecords();
            submission.setId(StrUtil.uuid());
            submission.setUserId(userId);
            submission.setCompetitionId(competitionId);
            submission.setTitle(title);
            submission.setDescription(description);
            submission.setFileName(fileName);
            submission.setFileUrl(uploadedUrl);
            submission.setFileType(fileType);
            submission.setReviewStatus("PENDING");

            boolean saved = this.save(submission);
            if (!saved) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save submission");
            }
        }

        UserBriefVO user = userServiceClient.getUserBriefById(userId).getBody();

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found");
        }

        SubmissionUploadedMessage message = new SubmissionUploadedMessage();
        message.setUserName(user.getName());
        message.setUserEmail(user.getEmail());
        message.setCompetitionName(competition.getName());
        message.setTitle(title);
        message.setSubmittedAt(LocalDateTime.now());
        submissionNotifier.sendSubmissionUploaded(message);
    }

    @Override
    public SubmissionInfoVO getMySubmission(String competitionId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT can view their submission");
        }

        SubmissionRecords submission = lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getUserId, userId)
                .one();

        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No submission found");
        }

        SubmissionInfoVO vo = new SubmissionInfoVO();
        BeanUtil.copyProperties(submission, vo);
        return vo;
    }

    @Override
    public PageResponse<SubmissionInfoVO> listSubmissionsByRole(
            String competitionId,
            String userId,
            String userRole,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order
    ) {
        boolean isOrganizerOrAdmin = "ADMIN".equalsIgnoreCase(userRole) ||
                competitionOrganizersService.lambdaQuery()
                        .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                        .eq(CompetitionOrganizers::getUserId, userId)
                        .exists();

        if (!isOrganizerOrAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers or admins can view all submissions");
        }

        List<SubmissionRecords> allSubmissions = lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .list();

        if (allSubmissions.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        List<SubmissionInfoVO> vos = allSubmissions.stream()
                .map(submission -> {
                    SubmissionInfoVO vo = new SubmissionInfoVO();
                    BeanUtil.copyProperties(submission, vo);
                    return vo;
                }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            vos = vos.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getTitle(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getDescription(), keyword))
                    .toList();
        }

        Comparator<SubmissionInfoVO> comparator = switch (sortBy) {
            case "title" -> Comparator.comparing(SubmissionInfoVO::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "createdAt" -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
            default -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        vos = vos.stream().sorted(comparator).toList();

        int total = vos.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SubmissionInfoVO> paged = vos.subList(fromIndex, toIndex);

        return new PageResponse<>(paged, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<SubmissionInfoVO> listPublicApprovedSubmissions(
            String competitionId, int page, int size, String keyword, String sortBy, String order) {

        List<SubmissionRecords> approved = lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "APPROVED")
                .list();

        if (approved.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        List<SubmissionInfoVO> vos = approved.stream()
                .map(submission -> {
                    SubmissionInfoVO vo = new SubmissionInfoVO();
                    BeanUtil.copyProperties(submission, vo);
                    return vo;
                }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            vos = vos.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getTitle(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getDescription(), keyword))
                    .toList();
        }

        Comparator<SubmissionInfoVO> comparator = switch (sortBy) {
            case "title" -> Comparator.comparing(SubmissionInfoVO::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "createdAt" -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
            default -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        vos = vos.stream().sorted(comparator).toList();

        int total = vos.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SubmissionInfoVO> paged = vos.subList(fromIndex, toIndex);

        return new PageResponse<>(paged, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    @Transactional
    public void reviewSubmission(SubmissionReviewDTO dto, String reviewerId, String reviewerRole) {
        SubmissionRecords submission = this.getById(dto.getSubmissionId());
        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Submission not found");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(reviewerRole);
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, submission.getCompetitionId())
                .eq(CompetitionOrganizers::getUserId, reviewerId)
                .exists();

        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to review this submission");
        }

        if (!"APPROVED".equalsIgnoreCase(dto.getReviewStatus()) &&
                !"REJECTED".equalsIgnoreCase(dto.getReviewStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid review status, must be APPROVED or REJECTED");
        }

        submission.setReviewStatus(dto.getReviewStatus().toUpperCase());
        submission.setReviewComments(dto.getReviewComments());
        submission.setReviewedBy(reviewerId);
        submission.setReviewedAt(LocalDateTime.now());

        boolean updated = this.updateById(submission);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update submission review status");
        }

        CompetitionResponseVO competition = Optional.ofNullable(
                competitionServiceClient.getCompetitionById(submission.getCompetitionId()).getBody()
        ).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Competition not found"));

        UserBriefVO reviewer = Optional.ofNullable(
                userServiceClient.getUserBriefById(reviewerId).getBody()
        ).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Reviewer info not found"));

        String notifyUserId;
        if (StrUtil.isNotBlank(submission.getTeamId())) {
            notifyUserId = Optional.ofNullable(
                            userServiceClient.getTeamCreator(submission.getTeamId()).getBody()
                    ).map(UserBriefVO::getId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Team creator not found"));
        } else {
            notifyUserId = submission.getUserId();
        }

        UserBriefVO submitter = Optional.ofNullable(
                userServiceClient.getUserBriefById(notifyUserId).getBody()
        ).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Submitter info not found"));

        SubmissionReviewedMessage message = new SubmissionReviewedMessage();
        message.setUserName(submitter.getName());
        message.setUserEmail(submitter.getEmail());
        message.setCompetitionName(competition.getName());
        message.setTitle(submission.getTitle());
        message.setReviewStatus(submission.getReviewStatus());
        message.setReviewComments(submission.getReviewComments());
        message.setReviewedAt(submission.getReviewedAt());
        message.setReviewedBy(reviewer.getName());

        submissionNotifier.sendSubmissionReviewed(message);
    }

    @Override
    public boolean isUserOrganizerOfSubmission(String submissionId, String userId) {
        SubmissionRecords submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Submission not found");
        }

        return competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, submission.getCompetitionId())
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();
    }

    @Override
    @Transactional
    public void deleteSubmission(String submissionId, String userId, String userRole) {
        SubmissionRecords submission = this.getById(submissionId);
        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Submission not found");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOwner = userId.equals(submission.getUserId());
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, submission.getCompetitionId())
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();

        if (!(isAdmin || isOwner || isOrganizer)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not allowed to delete this submission");
        }

        deleteFileByUrl(submission.getFileUrl());

        boolean removed = this.removeById(submissionId);
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete submission");
        }
    }

    @Override
    public Map<String, Boolean> getSubmissionStatusByTeam(List<String> teamIds, List<String> competitionIds) {
        if (CollUtil.isEmpty(teamIds) || CollUtil.isEmpty(competitionIds)) {
            return Collections.emptyMap();
        }

        return this.lambdaQuery()
                .in(SubmissionRecords::getTeamId, teamIds)
                .in(SubmissionRecords::getCompetitionId, competitionIds)
                .select(SubmissionRecords::getCompetitionId, SubmissionRecords::getTeamId)
                .list()
                .stream()
                .collect(Collectors.toMap(
                        s -> s.getCompetitionId() + ":" + s.getTeamId(),
                        s -> true,
                        (a, b) -> true
                ));
    }

    @Override
    public Map<String, BigDecimal> getSubmissionScoresByTeam(List<String> teamIds, List<String> competitionIds) {
        if (CollUtil.isEmpty(teamIds) || CollUtil.isEmpty(competitionIds)) {
            return Collections.emptyMap();
        }

        return this.lambdaQuery()
                .in(SubmissionRecords::getTeamId, teamIds)
                .in(SubmissionRecords::getCompetitionId, competitionIds)
                .eq(SubmissionRecords::getReviewStatus, "APPROVED")
                .select(SubmissionRecords::getCompetitionId, SubmissionRecords::getTeamId, SubmissionRecords::getTotalScore)
                .list()
                .stream()
                .filter(s -> s.getTotalScore() != null)
                .collect(Collectors.toMap(
                        s -> s.getCompetitionId() + ":" + s.getTeamId(),
                        SubmissionRecords::getTotalScore,
                        (a, b) -> a
                ));
    }

    @Override
    @Transactional
    public void submitTeamWork(
            String userId,
            String userRole,
            String competitionId,
            String teamId,
            String title,
            String description,
            MultipartFile file) {

        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can submit team work.");
        }

        Boolean isMember = Optional.ofNullable(userServiceClient.isUserInTeam(userId, teamId).getBody())
                .orElse(false);
        if (!isMember) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not a member of this team.");
        }

        CompetitionResponseVO competition = Optional.ofNullable(
                competitionServiceClient.getCompetitionById(competitionId).getBody()
        ).orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "Competition not found."));

        if (!CompetitionStatus.isSubmittable(competition.getStatus())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition is not open for submissions.");
        }
        if (competition.getEndDate() != null && competition.getEndDate().isBefore(LocalDateTime.now())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition has already ended.");
        }

        String fileUrl = Optional.ofNullable(fileServiceClient.uploadSubmission(file).getBody())
                .filter(StrUtil::isNotBlank)
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload file."));

        String fileName = file.getOriginalFilename();
        String fileType = file.getContentType();

        SubmissionRecords existing = lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getTeamId, teamId)
                .one();

        if (existing != null) {
            if (StrUtil.isNotBlank(existing.getFileUrl())) {
                deleteFileByUrl(existing.getFileUrl());
            }

            existing.setTitle(title)
                    .setDescription(description)
                    .setFileName(fileName)
                    .setFileUrl(fileUrl)
                    .setFileType(fileType)
                    .setUpdatedAt(LocalDateTime.now())
                    .setReviewStatus("PENDING")
                    .setReviewedBy(null)
                    .setReviewedAt(null)
                    .setReviewComments(null)
                    .setTotalScore(null);

            boolean updated = this.updateById(existing);
            if (!updated) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update existing team submission.");
            }

        } else {
            SubmissionRecords submission = new SubmissionRecords();
            submission.setId(StrUtil.uuid());
            submission.setCompetitionId(competitionId);
            submission.setTeamId(teamId);
            submission.setTitle(title);
            submission.setDescription(description);
            submission.setFileName(fileName);
            submission.setFileUrl(fileUrl);
            submission.setFileType(fileType);
            submission.setReviewStatus("PENDING");
            submission.setCreatedAt(LocalDateTime.now());

            boolean saved = this.save(submission);
            if (!saved) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save new team submission.");
            }
        }

        UserBriefVO user = Optional.ofNullable(userServiceClient.getUserBriefById(userId).getBody())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "User info not found."));

        SubmissionUploadedMessage message = new SubmissionUploadedMessage();
        message.setUserName(user.getName());
        message.setUserEmail(user.getEmail());
        message.setCompetitionName(competition.getName());
        message.setTitle(title);
        message.setSubmittedAt(LocalDateTime.now());

        submissionNotifier.sendSubmissionUploaded(message);
    }

    @Override
    public TeamSubmissionInfoVO getTeamSubmissionPublic(String competitionId, String teamId) {
        SubmissionRecords submission = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getTeamId, teamId)
                .one();

        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Submission not found for the specified team.");
        }

        TeamSubmissionInfoVO vo = new TeamSubmissionInfoVO();
        vo.setSubmissionId(submission.getId());
        vo.setCompetitionId(submission.getCompetitionId());
        vo.setTeamId(submission.getTeamId());
        vo.setTitle(submission.getTitle());
        vo.setDescription(submission.getDescription());
        vo.setFileName(submission.getFileName());
        vo.setFileUrl(submission.getFileUrl());
        vo.setFileType(submission.getFileType());
        vo.setCreatedAt(submission.getCreatedAt());
        vo.setReviewStatus(submission.getReviewStatus());
        vo.setReviewComments(submission.getReviewComments());
        vo.setReviewedBy(submission.getReviewedBy());
        vo.setReviewedAt(submission.getReviewedAt());
        vo.setTotalScore(submission.getTotalScore() != null ? submission.getTotalScore().doubleValue() : null);
        return vo;
    }

    @Override
    @Transactional
    public void deleteTeamSubmission(String submissionId, String userId, String userRole) {
        SubmissionRecords submission = this.getById(submissionId);

        if (submission == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Submission not found.");
        }

        if (!"ADMIN".equalsIgnoreCase(userRole)) {
            if (StrUtil.isBlank(submission.getTeamId())) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "This is not a team submission.");
            }

            Boolean isMember = Optional.ofNullable(userServiceClient.isUserInTeam(userId, submission.getTeamId()).getBody())
                    .orElse(false);
            if (!isMember) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to delete this submission.");
            }
        }

        if (StrUtil.isNotBlank(submission.getFileUrl())) {
            deleteFileByUrl(submission.getFileUrl());
        }

        boolean removed = this.removeById(submissionId);
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the team submission.");
        }
    }

    @Override
    public PageResponse<SubmissionInfoVO> listTeamSubmissionsByRole(
            String competitionId,
            String userId,
            String userRole,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order) {

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();

        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers or admins can view team submissions.");
        }

        List<SubmissionRecords> allTeamSubmissions = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .isNotNull(SubmissionRecords::getTeamId)
                .list();

        if (CollUtil.isEmpty(allTeamSubmissions)) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        List<SubmissionInfoVO> vos = allTeamSubmissions.stream()
                .map(submission -> {
                    SubmissionInfoVO vo = new SubmissionInfoVO();
                    BeanUtil.copyProperties(submission, vo);
                    return vo;
                }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            vos = vos.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getTitle(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getDescription(), keyword))
                    .toList();
        }

        Comparator<SubmissionInfoVO> comparator = switch (sortBy) {
            case "title" -> Comparator.comparing(SubmissionInfoVO::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "createdAt" -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
            default -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        vos = vos.stream().sorted(comparator).toList();

        int total = vos.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SubmissionInfoVO> pagedList = vos.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<SubmissionInfoVO> listPublicApprovedTeamSubmissions(
            String competitionId,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order) {

        List<SubmissionRecords> approvedTeamSubmissions = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "APPROVED")
                .isNotNull(SubmissionRecords::getTeamId)
                .list();

        if (CollUtil.isEmpty(approvedTeamSubmissions)) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        List<SubmissionInfoVO> vos = approvedTeamSubmissions.stream()
                .map(submission -> {
                    SubmissionInfoVO vo = new SubmissionInfoVO();
                    BeanUtil.copyProperties(submission, vo);
                    return vo;
                }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            vos = vos.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getTitle(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getDescription(), keyword))
                    .toList();
        }

        Comparator<SubmissionInfoVO> comparator = switch (sortBy) {
            case "title" -> Comparator.comparing(SubmissionInfoVO::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "createdAt" -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
            default -> Comparator.comparing(SubmissionInfoVO::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        vos = vos.stream().sorted(comparator).toList();

        int total = vos.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<SubmissionInfoVO> pagedList = vos.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public Boolean existsByTeamId(String teamId) {
        if (StrUtil.isBlank(teamId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Team ID must not be blank.");
        }

        return this.lambdaQuery()
                .eq(SubmissionRecords::getTeamId, teamId)
                .exists();
    }

    @Override
    public SubmissionStatisticsVO getSubmissionStatistics(String competitionId) {
        if (StrUtil.isBlank(competitionId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition ID must not be blank.");
        }

        long totalSubmissions = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .count();

        long approvedCount = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "APPROVED")
                .count();

        long pendingCount = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "PENDING")
                .count();

        long rejectedCount = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getReviewStatus, "REJECTED")
                .count();

        SubmissionStatisticsVO vo = new SubmissionStatisticsVO();
        vo.setTotalSubmissions((int) totalSubmissions);
        vo.setApprovedSubmissions((int) approvedCount);
        vo.setPendingSubmissions((int) pendingCount);
        vo.setRejectedSubmissions((int) rejectedCount);

        return vo;
    }

    @Override
    public Map<String, Integer> getSubmissionTrend(String competitionId) {
        if (StrUtil.isBlank(competitionId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition ID must not be blank.");
        }

        var competitionResp = competitionServiceClient.getCompetitionById(competitionId);
        var competition = competitionResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        List<SubmissionRecords> submissions = this.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .select(SubmissionRecords::getCreatedAt)
                .list();

        Map<String, Integer> trend = new TreeMap<>();
        for (SubmissionRecords submission : submissions) {
            if (submission.getCreatedAt() != null) {
                String date = submission.getCreatedAt().toLocalDate().toString();
                trend.merge(date, 1, Integer::sum);
            }
        }

        return trend;
    }

    @Override
    public PlatformSubmissionStatisticsVO getPlatformSubmissionStatistics() {
        List<SubmissionRecords> allSubmissions = this.lambdaQuery()
                .select(SubmissionRecords::getId, SubmissionRecords::getReviewStatus, SubmissionRecords::getTeamId)
                .list();

        if (CollUtil.isEmpty(allSubmissions)) {
            PlatformSubmissionStatisticsVO empty = new PlatformSubmissionStatisticsVO();
            empty.setTotalSubmissions(0);
            empty.setApprovedSubmissions(0);
            empty.setIndividualSubmissions(0);
            empty.setTeamSubmissions(0);
            return empty;
        }

        int totalSubmissions = allSubmissions.size();
        int approvedSubmissions = 0;
        int individualSubmissions = 0;
        int teamSubmissions = 0;

        for (SubmissionRecords submission : allSubmissions) {
            if ("APPROVED".equalsIgnoreCase(submission.getReviewStatus())) {
                approvedSubmissions++;
            }
            if (StrUtil.isBlank(submission.getTeamId())) {
                individualSubmissions++;
            } else {
                teamSubmissions++;
            }
        }

        PlatformSubmissionStatisticsVO vo = new PlatformSubmissionStatisticsVO();
        vo.setTotalSubmissions(totalSubmissions);
        vo.setApprovedSubmissions(approvedSubmissions);
        vo.setIndividualSubmissions(individualSubmissions);
        vo.setTeamSubmissions(teamSubmissions);

        return vo;
    }

    @Override
    public Map<String, Integer> getPlatformSubmissionTrend() {
        List<SubmissionRecords> submissions = this.lambdaQuery()
                .select(SubmissionRecords::getCreatedAt)
                .list();

        if (CollUtil.isEmpty(submissions)) {
            return Collections.emptyMap();
        }

        Map<String, Integer> trend = new TreeMap<>();

        for (SubmissionRecords submission : submissions) {
            if (submission.getCreatedAt() != null) {
                String date = submission.getCreatedAt().toLocalDate().toString();
                trend.merge(date, 1, Integer::sum);
            }
        }

        return trend;
    }

    private void deleteFileByUrl(String fileUrl) {
        if (StrUtil.isBlank(fileUrl)) {
            return;
        }
        URI uri = URI.create(fileUrl);
        String[] parts = uri.getPath().substring(1).split("/", 2);
        if (parts.length == 2) {
            fileServiceClient.deleteFile(parts[0], parts[1]);
        }
    }
}
