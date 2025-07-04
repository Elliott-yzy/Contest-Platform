package com.w16a.danish.competition.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.w16a.danish.competition.config.CompetitionNotifier;
import com.w16a.danish.competition.domain.dto.AssignJudgesDTO;
import com.w16a.danish.competition.domain.dto.CompetitionCreateDTO;
import com.w16a.danish.competition.domain.dto.CompetitionUpdateDTO;
import com.w16a.danish.competition.domain.enums.CompetitionStatus;
import com.w16a.danish.competition.domain.mq.JudgeAssignedMessage;
import com.w16a.danish.competition.domain.mq.JudgeRemovedMessage;
import com.w16a.danish.competition.domain.po.CompetitionJudges;
import com.w16a.danish.competition.domain.po.CompetitionOrganizers;
import com.w16a.danish.competition.domain.po.Competitions;
import com.w16a.danish.competition.domain.vo.CompetitionResponseVO;
import com.w16a.danish.competition.domain.vo.PageResponse;
import com.w16a.danish.competition.domain.vo.UserBriefVO;
import com.w16a.danish.competition.exception.BusinessException;
import com.w16a.danish.competition.feign.FileServiceClient;
import com.w16a.danish.competition.feign.UserServiceClient;
import com.w16a.danish.competition.mapper.CompetitionsMapper;
import com.w16a.danish.competition.service.ICompetitionJudgesService;
import com.w16a.danish.competition.service.ICompetitionOrganizersService;
import com.w16a.danish.competition.service.ICompetitionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Eddy ZHANG
 * @date 2025/03/18
 * @description ServiceImpl class for Competitions
 */
@Service
@RequiredArgsConstructor
public class CompetitionsServiceImpl extends ServiceImpl<CompetitionsMapper, Competitions> implements ICompetitionsService {

    private static final List<String> VIDEO_CONTENT_TYPES = List.of("video/mp4", "video/avi", "video/mov");
    private static final List<String> IMAGE_CONTENT_TYPES = List.of("image/jpeg", "image/png", "image/gif");

    private final FileServiceClient fileServiceClient;
    private final ICompetitionOrganizersService competitionOrganizersService;
    private final UserServiceClient userServiceClient;
    private final ICompetitionJudgesService competitionJudgesService;
    private final CompetitionNotifier competitionNotifier;

    @Override
    @Transactional
    public CompetitionResponseVO createCompetition(CompetitionCreateDTO competitionDTO, String currentUserRole, String currentUserId) {
        // validate user role
        if (!"ADMIN".equalsIgnoreCase(currentUserRole) && !"ORGANIZER".equalsIgnoreCase(currentUserRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to create a competition");
        }

        // check if competition name already exists
        boolean exists = lambdaQuery()
                .eq(Competitions::getName, competitionDTO.getName())
                .exists();

        if (exists) {
            throw new BusinessException(HttpStatus.CONFLICT, "A competition with the same name already exists");
        }

        // create competition object
        Competitions competition = new Competitions();
        BeanUtils.copyProperties(competitionDTO, competition);
        competition.setId(StrUtil.uuid());
        if (competitionDTO.getStatus() != null) {
            try {
                competition.setStatus(CompetitionStatus.fromString(competitionDTO.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid competition status: " + competitionDTO.getStatus());
            }
        } else {
            competition.setStatus(CompetitionStatus.UPCOMING);
        }
        save(competition);

        // Insert the user-competition mapping (organizer relationship) into competition_organizers table
        CompetitionOrganizers competitionOrganizers = new CompetitionOrganizers();
        competitionOrganizers.setId(StrUtil.uuid());
        competitionOrganizers.setCompetitionId(competition.getId());
        competitionOrganizers.setUserId(currentUserId);
        competitionOrganizersService.save(competitionOrganizers);

        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtils.copyProperties(competition, responseVO);
        return responseVO;
    }

    @Override
    @Transactional
    public void deleteCompetition(String competitionId, String currentUserRole, String currentUserId) {
        if (!"ADMIN".equalsIgnoreCase(currentUserRole) && !"ORGANIZER".equalsIgnoreCase(currentUserRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to delete a competition");
        }

        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        if ("ORGANIZER".equalsIgnoreCase(currentUserRole)) {
            boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                    .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                    .eq(CompetitionOrganizers::getUserId, currentUserId)
                    .exists();

            if (!isOrganizer) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to delete this competition");
            }
        }

        competitionOrganizersService.remove(new LambdaQueryWrapper<CompetitionOrganizers>().eq(CompetitionOrganizers::getCompetitionId, competitionId));

        boolean isRemoved = this.removeById(competitionId);
        if (!isRemoved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete competition");
        }
    }

    @Override
    public CompetitionResponseVO getCompetitionById(String competitionId) {
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found with ID: " + competitionId);
        }

        CompetitionResponseVO response = new CompetitionResponseVO();
        BeanUtils.copyProperties(competition, response);
        return response;
    }

    @Override
    public PageResponse<CompetitionResponseVO> listCompetitions(String keyword, String status, String category, int page, int size) {
        LambdaQueryWrapper<Competitions> wrapper = new LambdaQueryWrapper<>();

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Competitions::getName, keyword);
        }

        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Competitions::getStatus, status);
        }

        if (StrUtil.isNotBlank(category)) {
            wrapper.eq(Competitions::getCategory, category);
        }

        wrapper.eq(Competitions::getIsPublic, true);

        wrapper.orderByDesc(Competitions::getCreatedAt);

        Page<Competitions> mpPage = new Page<>(page, size);
        IPage<Competitions> resultPage = this.page(mpPage, wrapper);

        List<CompetitionResponseVO> voList = resultPage.getRecords().stream()
                .map(entity -> {
                    CompetitionResponseVO vo = new CompetitionResponseVO();
                    BeanUtil.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        return PageResponse.<CompetitionResponseVO>builder()
                .data(voList)
                .total(resultPage.getTotal())
                .page(resultPage.getCurrent())
                .size(resultPage.getSize())
                .pages(resultPage.getPages())
                .build();
    }

    @Override
    @Transactional
    public CompetitionResponseVO updateCompetition(String competitionId, String userId, String userRole, CompetitionUpdateDTO updateDTO) {
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOrganizer = "ORGANIZER".equalsIgnoreCase(userRole) &&
                competitionOrganizersService.lambdaQuery()
                        .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                        .eq(CompetitionOrganizers::getUserId, userId)
                        .exists();

        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to update this competition");
        }

        BeanUtil.copyProperties(updateDTO, competition, CopyOptions.create().ignoreNullValue());

        if (StrUtil.isNotBlank(updateDTO.getStatus())) {
            competition.setStatus(CompetitionStatus.fromString(updateDTO.getStatus()));
        }

        competition.setUpdatedAt(null);

        boolean updated = this.updateById(competition);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update competition");
        }

        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtils.copyProperties(competition, responseVO);
        return responseVO;
    }

    @Override
    @Transactional
    public CompetitionResponseVO uploadCompetitionMedia(String competitionId, String userId, String userRole, String mediaType, MultipartFile file) {
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        if ("ORGANIZER".equalsIgnoreCase(userRole)) {
            boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                    .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                    .eq(CompetitionOrganizers::getUserId, userId)
                    .exists();
            if (!isOrganizer) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to upload media for this competition");
            }
        } else if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to upload media for this competition");
        }

        String contentType = file.getContentType();
        if (StrUtil.isBlank(contentType)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot detect file content type");
        }

        if ("VIDEO".equalsIgnoreCase(mediaType)) {
            if (!VIDEO_CONTENT_TYPES.contains(contentType)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid video type. Allowed: MP4, AVI, MOV");
            }
            deleteFileByUrl(competition.getIntroVideoUrl());
        } else if ("IMAGE".equalsIgnoreCase(mediaType)) {
            if (!IMAGE_CONTENT_TYPES.contains(contentType)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid image type. Allowed: JPG, PNG, GIF");
            }
        } else {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid media type. Supported: VIDEO or IMAGE");
        }

        String uploadedUrl = fileServiceClient.uploadCompetitionPromo(file).getBody();

        if ("VIDEO".equalsIgnoreCase(mediaType)) {
            competition.setIntroVideoUrl(uploadedUrl);
        } else {
            List<String> imageList = Optional.ofNullable(competition.getImageUrls()).orElse(new ArrayList<>());
            imageList.add(uploadedUrl);
            competition.setImageUrls(imageList);
        }

        competition.setUpdatedAt(null);
        this.updateById(competition);

        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtil.copyProperties(competition, responseVO);
        return responseVO;
    }

    @Override
    @Transactional
    public CompetitionResponseVO deleteCompetitionImage(String competitionId, String userId, String userRole, String imageUrl) {
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        if ("ORGANIZER".equalsIgnoreCase(userRole)) {
            boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                    .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                    .eq(CompetitionOrganizers::getUserId, userId)
                    .exists();
            if (!isOrganizer) {
                throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to modify this competition");
            }
        } else if (!"ADMIN".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to modify this competition");
        }

        List<String> currentImages = Optional.ofNullable(competition.getImageUrls()).orElse(new ArrayList<>());
        if (!currentImages.remove(imageUrl)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Image URL not found in competition");
        }

        deleteFileByUrl(imageUrl);

        competition.setImageUrls(currentImages);
        competition.setUpdatedAt(null);
        this.updateById(competition);

        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtil.copyProperties(competition, responseVO);
        return responseVO;
    }

    @Override
    public PageResponse<CompetitionResponseVO> listCompetitionsByOrganizer(String userId, String userRole, int page, int size) {
        if (!"ORGANIZER".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers can access their own competitions");
        }

        List<String> competitionIds = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getUserId, userId)
                .list()
                .stream()
                .map(CompetitionOrganizers::getCompetitionId)
                .toList();

        if (competitionIds.isEmpty()) {
            return PageResponse.<CompetitionResponseVO>builder()
                    .data(new ArrayList<>())
                    .total(0)
                    .page(page)
                    .size(size)
                    .pages(0)
                    .build();
        }

        LambdaQueryWrapper<Competitions> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Competitions::getId, competitionIds)
                .orderByDesc(Competitions::getCreatedAt);

        Page<Competitions> mpPage = new Page<>(page, size);
        IPage<Competitions> resultPage = this.page(mpPage, wrapper);

        List<CompetitionResponseVO> voList = resultPage.getRecords().stream()
                .map(c -> BeanUtil.copyProperties(c, CompetitionResponseVO.class))
                .collect(Collectors.toList());

        return PageResponse.<CompetitionResponseVO>builder()
                .data(voList)
                .total(resultPage.getTotal())
                .page((int) resultPage.getCurrent())
                .size((int) resultPage.getSize())
                .pages((int) resultPage.getPages())
                .build();
    }

    @Override
    @Transactional
    public CompetitionResponseVO deleteIntroVideo(String competitionId, String userId, String userRole) {
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        if (!isAuthorizedToModify(competitionId, userId, userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to modify this competition");
        }

        String introVideoUrl = competition.getIntroVideoUrl();
        if (StrUtil.isBlank(introVideoUrl)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "No intro video to delete");
        }

        deleteFileByUrl(introVideoUrl);

        UpdateWrapper<Competitions> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", competitionId)
                .set("intro_video_url", null)
                .set("updated_at", LocalDateTime.now());

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update competition after deleting video");
        }

        Competitions updatedCompetition = this.getById(competitionId);
        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtil.copyProperties(updatedCompetition, responseVO);
        return responseVO;
    }

    @Override
    public List<CompetitionResponseVO> getCompetitionsByIds(List<String> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }

        List<Competitions> competitions = this.lambdaQuery()
                .in(Competitions::getId, ids)
                .list();

        return competitions.stream()
                .map(c -> {
                    CompetitionResponseVO vo = new CompetitionResponseVO();
                    BeanUtil.copyProperties(c, vo);
                    return vo;
                })
                .toList();
    }

    @Override
    @Transactional
    public void assignJudges(String competitionId, String userId, String userRole, AssignJudgesDTO dto) {
        // Step 1: Validate competition existence
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        // Step 2: Check permission - Only ADMIN or the assigned ORGANIZER can assign judges
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();

        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to assign judges to this competition.");
        }

        // Step 3: Validate input emails
        if (dto.getJudgeEmails() == null || dto.getJudgeEmails().isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Judge email list cannot be empty.");
        }

        // Step 4: Query users by emails
        List<UserBriefVO> users = Optional.ofNullable(userServiceClient.getUsersByEmails(dto.getJudgeEmails()).getBody())
                .orElse(Collections.emptyList());

        if (users.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "No valid users found for provided emails.");
        }

        // Step 5: Get already assigned judges to avoid duplication
        List<String> existingJudgeUserIds = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .select(CompetitionJudges::getUserId)
                .list()
                .stream()
                .map(CompetitionJudges::getUserId)
                .toList();

        // Step 6: Filter out already assigned users
        List<CompetitionJudges> judgeList = users.stream()
                .filter(user -> !existingJudgeUserIds.contains(user.getId()))
                .map(user -> new CompetitionJudges()
                        .setId(StrUtil.uuid())
                        .setCompetitionId(competitionId)
                        .setUserId(user.getId()))
                .collect(Collectors.toList());

        if (judgeList.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "All users are already assigned as judges.");
        }

        // Step 7: Batch save judge records
        boolean saved = competitionJudgesService.saveBatch(judgeList);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to assign judges.");
        }

        // Step 8: After successful assignment, send MQ notification to each newly assigned judge
        for (UserBriefVO user : users) {
            if (!existingJudgeUserIds.contains(user.getId())) {
                JudgeAssignedMessage message = new JudgeAssignedMessage();
                message.setJudgeName(user.getName());
                message.setJudgeEmail(user.getEmail());
                message.setCompetitionName(competition.getName());
                message.setAssignedAt(LocalDateTime.now());

                competitionNotifier.sendJudgeAssigned(message);
            }
        }
    }

    @Override
    public PageResponse<UserBriefVO> listAssignedJudges(String competitionId, String userId, String userRole, int page, int size) {
        // Step 1: Check if competition exists
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        // Step 2: Validate user permission
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();
        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to view assigned judges.");
        }

        // Step 3: Query assigned judge user IDs from competition_judges table
        Page<CompetitionJudges> judgePage = new Page<>(page, size);
        Page<CompetitionJudges> resultPage = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .page(judgePage);

        List<String> judgeUserIds = resultPage.getRecords().stream()
                .map(CompetitionJudges::getUserId)
                .toList();

        if (judgeUserIds.isEmpty()) {
            // No assigned judges, return empty result
            return PageResponse.<UserBriefVO>builder()
                    .data(Collections.emptyList())
                    .total(0)
                    .page(page)
                    .size(size)
                    .pages(0)
                    .build();
        }

        // Step 4: Fetch user brief info for judgeUserIds
        List<UserBriefVO> userBriefs = userServiceClient.getUsersByIds(judgeUserIds, null).getBody();
        if (userBriefs == null) {
            userBriefs = Collections.emptyList();
        }

        // Step 5: Construct the paginated response
        return PageResponse.<UserBriefVO>builder()
                .data(userBriefs)
                .total(resultPage.getTotal())
                .page((int) resultPage.getCurrent())
                .size((int) resultPage.getSize())
                .pages((int) resultPage.getPages())
                .build();
    }

    @Override
    @Transactional
    public void removeJudge(String competitionId, String userId, String userRole, String judgeId) {
        // Step 1: Check if competition exists
        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        // Step 2: Validate user permission (must be ADMIN or Organizer of this competition)
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();
        if (!isAdmin && !isOrganizer) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to remove judges.");
        }

        // Step 3: Check if the judge is actually assigned to the competition
        boolean exists = competitionJudgesService.lambdaQuery()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .eq(CompetitionJudges::getUserId, judgeId)
                .exists();
        if (!exists) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Judge is not assigned to this competition.");
        }

        // Step 4: Remove the judge assignment
        boolean removed = competitionJudgesService.lambdaUpdate()
                .eq(CompetitionJudges::getCompetitionId, competitionId)
                .eq(CompetitionJudges::getUserId, judgeId)
                .remove();
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove judge from competition.");
        }

        UserBriefVO user = userServiceClient.getUserBriefById(judgeId).getBody();

        if (user != null) {
            // Step 6: Send MQ notification
            JudgeRemovedMessage message = new JudgeRemovedMessage();
            message.setJudgeName(user.getName());
            message.setJudgeEmail(user.getEmail());
            message.setCompetitionName(competition.getName());
            message.setRemovedAt(LocalDateTime.now());

            competitionNotifier.sendJudgeRemoved(message);
        }
    }

    @Override
    public boolean isUserOrganizer(String competitionId, String userId) {
        return competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();
    }

    @Override
    public List<CompetitionResponseVO> listAllCompetitions() {
        List<Competitions> competitions = this.lambdaQuery()
                .eq(Competitions::getIsPublic, true)
                .orderByDesc(Competitions::getCreatedAt)
                .list();

        if (CollUtil.isEmpty(competitions)) {
            return Collections.emptyList();
        }

        return competitions.stream()
                .map(entity -> {
                    CompetitionResponseVO vo = new CompetitionResponseVO();
                    BeanUtil.copyProperties(entity, vo);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CompetitionResponseVO updateCompetitionStatus(String competitionId, String newStatus) {
        if (StrUtil.isBlank(competitionId) || StrUtil.isBlank(newStatus)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition ID and status must not be blank.");
        }

        Competitions competition = this.getById(competitionId);
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
        }

        CompetitionStatus validatedStatus;
        try {
            validatedStatus = CompetitionStatus.fromString(newStatus);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Invalid competition status: " + newStatus);
        }

        competition.setStatus(validatedStatus);
        competition.setUpdatedAt(LocalDateTime.now());
        boolean updated = this.updateById(competition);

        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update competition status.");
        }

        CompetitionResponseVO responseVO = new CompetitionResponseVO();
        BeanUtil.copyProperties(competition, responseVO);
        return responseVO;
    }


    private boolean isAuthorizedToModify(String competitionId, String userId, String userRole) {
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return true;
        }
        if ("ORGANIZER".equalsIgnoreCase(userRole)) {
            return competitionOrganizersService.lambdaQuery()
                    .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                    .eq(CompetitionOrganizers::getUserId, userId)
                    .exists();
        }
        return false;
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
