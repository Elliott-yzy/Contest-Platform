package com.w16a.danish.registration.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.w16a.danish.registration.config.RegistrationNotifier;
import com.w16a.danish.registration.domain.mq.ParticipantRemovedMessage;
import com.w16a.danish.registration.domain.mq.RegisterSuccessMessage;
import com.w16a.danish.registration.domain.po.CompetitionOrganizers;
import com.w16a.danish.registration.domain.po.CompetitionParticipants;
import com.w16a.danish.registration.domain.po.CompetitionTeams;
import com.w16a.danish.registration.domain.po.SubmissionRecords;
import com.w16a.danish.registration.domain.vo.*;
import com.w16a.danish.registration.enums.CompetitionStatus;
import com.w16a.danish.registration.enums.ParticipationType;
import com.w16a.danish.registration.exception.BusinessException;
import com.w16a.danish.registration.feign.CompetitionServiceClient;
import com.w16a.danish.registration.feign.UserServiceClient;
import com.w16a.danish.registration.mapper.CompetitionParticipantsMapper;
import com.w16a.danish.registration.service.ICompetitionOrganizersService;
import com.w16a.danish.registration.service.ICompetitionParticipantsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.registration.service.ICompetitionTeamsService;
import com.w16a.danish.registration.service.ISubmissionRecordsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * This class handles the registration of participants for competitions.
 *
 * @author Eddy ZHANG
 * @date 2025/04/03
 */
@Service
@RequiredArgsConstructor
public class CompetitionParticipantsServiceImpl extends ServiceImpl<CompetitionParticipantsMapper, CompetitionParticipants> implements ICompetitionParticipantsService {

    private final CompetitionServiceClient competitionServiceClient;
    private final ICompetitionOrganizersService competitionOrganizersService;
    private final UserServiceClient userServiceClient;
    private final ISubmissionRecordsService submissionService;
    private final RegistrationNotifier registrationNotifier;
    private final ICompetitionTeamsService competitionTeamsService;

    @Override
    @Transactional
    public void register(String competitionId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can register for competitions");
        }

        CompetitionResponseVO competition;
        try {
            ResponseEntity<CompetitionResponseVO> response = competitionServiceClient.getCompetitionById(competitionId);
            competition = response.getBody();

            if (competition == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
            }

            if (!CompetitionStatus.isRegistrable(competition.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Registration is only allowed for UPCOMING or ONGOING competitions");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception e) {
            throw new BusinessException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Failed to verify competition with competition-service: " + e.getMessage()
            );
        }

        boolean alreadyRegistered = lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .eq(CompetitionParticipants::getUserId, userId)
                .exists();

        if (alreadyRegistered) {
            throw new BusinessException(HttpStatus.CONFLICT, "You have already registered for this competition");
        }

        CompetitionParticipants participant = new CompetitionParticipants()
                .setId(StrUtil.uuid())
                .setCompetitionId(competitionId)
                .setUserId(userId);

        boolean saved = this.save(participant);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register for the competition");
        }

        ResponseEntity<UserBriefVO> response = userServiceClient.getUserBriefById(userId);
        UserBriefVO user = response.getBody();

        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User not found when sending registration notification");
        }

        RegisterSuccessMessage message = new RegisterSuccessMessage();
        message.setUserName(user.getName());
        message.setUserEmail(user.getEmail());
        message.setCompetitionName(competition.getName());
        message.setRegisterTime(LocalDateTime.now());
        registrationNotifier.sendRegisterSuccess(message);

    }

    @Override
    @Transactional
    public void cancelRegistration(String competitionId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only participants can cancel registration");
        }

        CompetitionParticipants existing = lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .eq(CompetitionParticipants::getUserId, userId)
                .one();

        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "You have not registered for this competition");
        }

        boolean hasSubmission = submissionService.lambdaQuery()
                .eq(SubmissionRecords::getUserId, userId)
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .exists();

        if (hasSubmission) {
            submissionService.deleteSubmissionsByUserAndCompetition(userId, competitionId);
        }

        boolean removed = this.removeById(existing.getId());
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel registration");
        }
    }

    @Override
    public boolean isRegistered(String competitionId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can check registration status");
        }

        return lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .eq(CompetitionParticipants::getUserId, userId)
                .exists();
    }

    @Override
    public PageResponse<CompetitionParticipationVO> getMyCompetitionsWithSearch(
            String userId,
            String userRole,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order
    ) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can view registered competitions");
        }

        List<CompetitionParticipants> participants = lambdaQuery()
                .eq(CompetitionParticipants::getUserId, userId)
                .list();

        if (participants.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        Map<String, LocalDateTime> registeredAtMap = participants.stream()
                .collect(Collectors.toMap(CompetitionParticipants::getCompetitionId, CompetitionParticipants::getCreatedAt));
        List<String> competitionIds = new ArrayList<>(registeredAtMap.keySet());

        ResponseEntity<List<CompetitionResponseVO>> response = competitionServiceClient.getCompetitionsByIds(competitionIds);
        List<CompetitionResponseVO> competitions = Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());

        Map<String, Boolean> hasSubmittedMap = submissionService.getSubmissionStatus(userId, competitionIds);
        Map<String, BigDecimal> scoreMap = submissionService.getSubmissionScores(userId, competitionIds);

        List<CompetitionParticipationVO> all = competitions.stream().map(c -> {
            CompetitionParticipationVO vo = new CompetitionParticipationVO();
            vo.setCompetitionId(c.getId());
            vo.setCompetitionName(c.getName());
            vo.setCategory(c.getCategory());
            vo.setStatus(c.getStatus().getValue());
            vo.setStartDate(c.getStartDate());
            vo.setEndDate(c.getEndDate());
            vo.setIsPublic(c.getIsPublic());
            vo.setJoinedAt(registeredAtMap.get(c.getId()));
            vo.setHasSubmitted(hasSubmittedMap.getOrDefault(c.getId(), false));
            vo.setTotalScore(scoreMap.getOrDefault(c.getId(), null));
            return vo;
        }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            all = all.stream()
                    .filter(vo -> StrUtil.containsIgnoreCase(vo.getCompetitionName(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getCategory(), keyword))
                    .toList();
        }

        Comparator<CompetitionParticipationVO> comparator = switch (sortBy) {
            case "category" -> Comparator.comparing(CompetitionParticipationVO::getCategory, String.CASE_INSENSITIVE_ORDER);
            case "startDate" -> Comparator.comparing(CompetitionParticipationVO::getStartDate);
            case "endDate" -> Comparator.comparing(CompetitionParticipationVO::getEndDate);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "joinedAt" -> Comparator.comparing(CompetitionParticipationVO::getJoinedAt);
            default -> Comparator.comparing(CompetitionParticipationVO::getCompetitionName, String.CASE_INSENSITIVE_ORDER);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        all = all.stream().sorted(comparator).toList();

        int total = all.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<CompetitionParticipationVO> pagedList = all.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<ParticipantInfoVO> getParticipantsByCompetitionWithSearch(
            String competitionId,
            String organizerId,
            String userRole,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order
    ) {
        if (!"ORGANIZER".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only organizers can access this resource");
        }

        boolean isOwner = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, organizerId)
                .exists();

        if (!isOwner) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized for this competition");
        }

        // query participants
        List<CompetitionParticipants> participants = lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .list();

        if (participants.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        Map<String, LocalDateTime> userRegisteredAtMap = participants.stream()
                .collect(Collectors.toMap(CompetitionParticipants::getUserId, CompetitionParticipants::getCreatedAt));

        List<String> userIds = new ArrayList<>(userRegisteredAtMap.keySet());

        // safe check
        ResponseEntity<List<UserBriefVO>> response = userServiceClient.getUsersByIds(userIds, "PARTICIPANT");
        List<UserBriefVO> userList = Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());

        List<ParticipantInfoVO> allParticipants = userList.stream()
                .map(user -> {
                    ParticipantInfoVO vo = new ParticipantInfoVO();
                    vo.setUserId(user.getId());
                    vo.setName(user.getName());
                    vo.setEmail(user.getEmail());
                    vo.setAvatarUrl(user.getAvatarUrl());
                    vo.setDescription(user.getDescription());
                    vo.setRegisteredAt(userRegisteredAtMap.get(user.getId()));
                    return vo;
                })
                .toList();

        // search by keyword
        if (StrUtil.isNotBlank(keyword)) {
            allParticipants = allParticipants.stream()
                    .filter(u -> StrUtil.containsIgnoreCase(u.getName(), keyword)
                            || StrUtil.containsIgnoreCase(u.getEmail(), keyword))
                    .toList();
        }

        // sort by specified field
        Comparator<ParticipantInfoVO> comparator = switch (sortBy) {
            case "email" -> Comparator.comparing(ParticipantInfoVO::getEmail, String.CASE_INSENSITIVE_ORDER);
            case "registeredAt" -> Comparator.comparing(ParticipantInfoVO::getRegisteredAt);
            default -> Comparator.comparing(ParticipantInfoVO::getName, String.CASE_INSENSITIVE_ORDER);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        allParticipants = allParticipants.stream().sorted(comparator).toList();

        // pagination
        int total = allParticipants.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ParticipantInfoVO> pagedList = allParticipants.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    @Transactional
    public void cancelByOrganizer(String competitionId, String participantUserId, String organizerId, String userRole) {
        if (!"ORGANIZER".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only ORGANIZER role can perform this operation");
        }

        boolean isOwner = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, organizerId)
                .exists();

        if (!isOwner) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to manage this competition");
        }

        CompetitionParticipants existing = lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .eq(CompetitionParticipants::getUserId, participantUserId)
                .one();

        if (existing == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Participant is not registered for this competition");
        }

        boolean hasSubmission = submissionService.lambdaQuery()
                .eq(SubmissionRecords::getUserId, participantUserId)
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .exists();

        if (hasSubmission) {
            submissionService.deleteSubmissionsByUserAndCompetition(participantUserId, competitionId);
        }

        boolean removed = this.removeById(existing.getId());
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel participant registration");
        }

        ResponseEntity<UserBriefVO> userResp = userServiceClient.getUserBriefById(participantUserId);
        UserBriefVO participant = userResp.getBody();
        if (participant == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Participant info not found");
        }

        ResponseEntity<UserBriefVO> organizerResp = userServiceClient.getUserBriefById(organizerId);
        UserBriefVO organizer = organizerResp.getBody();
        if (organizer == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Organizer info not found");
        }

        ResponseEntity<CompetitionResponseVO> compResp = competitionServiceClient.getCompetitionById(competitionId);
        CompetitionResponseVO competition = compResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition info not found");
        }

        ParticipantRemovedMessage message = new ParticipantRemovedMessage();
        message.setUserName(participant.getName());
        message.setUserEmail(participant.getEmail());
        message.setRemovedBy(organizer.getName());
        message.setCompetitionName(competition.getName());
        message.setRemovedAt(LocalDateTime.now());
        registrationNotifier.sendParticipantRemoved(message);
    }

    @Override
    @Transactional
    public void registerTeam(String competitionId, String teamId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only PARTICIPANT role can register a team.");
        }

        ResponseEntity<UserBriefVO> creatorResponse = userServiceClient.getTeamCreator(teamId);
        UserBriefVO creator = creatorResponse.getBody();
        if (creator == null || !userId.equals(creator.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the team creator can register the team.");
        }

        CompetitionResponseVO competition;
        try {
            ResponseEntity<CompetitionResponseVO> response = competitionServiceClient.getCompetitionById(competitionId);
            competition = response.getBody();

            if (competition == null) {
                throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found.");
            }

            if (!CompetitionStatus.isRegistrable(competition.getStatus())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "Competition is not open for registration.");
            }

            if (competition.getParticipationType() != ParticipationType.TEAM) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "This competition only supports team registration.");
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to validate competition: " + e.getMessage());
        }

        boolean alreadyRegistered = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .eq(CompetitionTeams::getTeamId, teamId)
                .exists();

        if (alreadyRegistered) {
            throw new BusinessException(HttpStatus.CONFLICT, "This team has already registered for the competition.");
        }

        CompetitionTeams teamRegistration = new CompetitionTeams()
                .setId(StrUtil.uuid())
                .setCompetitionId(competitionId)
                .setTeamId(teamId);

        boolean saved = competitionTeamsService.save(teamRegistration);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register the team for the competition.");
        }

        ResponseEntity<UserBriefVO> userResponse = userServiceClient.getUserBriefById(userId);
        UserBriefVO user = userResponse.getBody();
        if (user == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "User info not found.");
        }

        RegisterSuccessMessage message = new RegisterSuccessMessage();
        message.setUserName(user.getName());
        message.setUserEmail(user.getEmail());
        message.setCompetitionName(competition.getName());
        message.setRegisterTime(LocalDateTime.now());

        registrationNotifier.sendRegisterSuccess(message);
    }


    @Override
    @Transactional
    public void cancelTeamRegistration(String competitionId, String teamId, String userId, String userRole) {
        if (!"PARTICIPANT".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only team creators (PARTICIPANT role) can cancel team registration.");
        }

        // Step 1: Validate team creator
        ResponseEntity<UserBriefVO> creatorResp = userServiceClient.getTeamCreator(teamId);
        UserBriefVO creator = creatorResp.getBody();
        if (creator == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team creator not found.");
        }
        if (!creator.getId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the team creator can cancel the registration.");
        }

        // Step 2: Query competition_teams registration record
        CompetitionTeams registration = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .eq(CompetitionTeams::getTeamId, teamId)
                .one();

        if (registration == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "This team is not registered for the competition.");
        }

        // Step 3: Delete all submissions made by the team for this competition
        boolean hasTeamSubmission = submissionService.lambdaQuery()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getTeamId, teamId)
                .exists();

        if (hasTeamSubmission) {
            submissionService.lambdaUpdate()
                    .eq(SubmissionRecords::getCompetitionId, competitionId)
                    .eq(SubmissionRecords::getTeamId, teamId)
                    .remove();
        }

        // Step 4: Remove team registration record from competition_teams
        boolean removed = competitionTeamsService.removeById(registration.getId());
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to cancel team registration.");
        }
    }

    @Override
    public boolean isTeamRegistered(String competitionId, String teamId) {
        // Check whether the team is already registered for the given competition
        return competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .eq(CompetitionTeams::getTeamId, teamId)
                .exists();
    }

    @Override
    public PageResponse<TeamInfoVO> getTeamsByCompetitionWithSearch(
            String competitionId,
            int page,
            int size,
            String keyword,
            String sortBy,
            String order
    ) {

        // Step 1: Query all registered teams for the competition from competition_teams
        List<CompetitionTeams> competitionTeams = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .list();

        if (competitionTeams.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        // Step 2: Extract unique teamIds
        List<String> teamIds = competitionTeams.stream()
                .map(CompetitionTeams::getTeamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // Step 3: Call user-service to get team brief info
        ResponseEntity<List<TeamInfoVO>> response = userServiceClient.getTeamBriefByIds(teamIds);
        List<TeamInfoVO> allTeams = Optional.ofNullable(response.getBody()).orElse(Collections.emptyList());

        // Step 4: Apply keyword filtering if needed
        if (StrUtil.isNotBlank(keyword)) {
            allTeams = allTeams.stream()
                    .filter(team ->
                            StrUtil.containsIgnoreCase(team.getTeamName(), keyword)
                                    || StrUtil.containsIgnoreCase(team.getDescription(), keyword))
                    .toList();
        }

        // Step 5: Sorting
        Comparator<TeamInfoVO> comparator = switch (sortBy) {
            case "createdAt" -> Comparator.comparing(TeamInfoVO::getCreatedAt);
            case "teamName" -> Comparator.comparing(TeamInfoVO::getTeamName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(TeamInfoVO::getCreatedAt);
        };

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        allTeams = allTeams.stream().sorted(comparator).toList();

        // Step 6: Pagination
        int total = allTeams.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<TeamInfoVO> pagedList = allTeams.subList(fromIndex, toIndex);

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<CompetitionParticipationVO> getCompetitionsRegisteredByTeam(
            String teamId, int page, int size, String keyword, String sortBy, String order) {

        List<CompetitionTeams> registrations = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getTeamId, teamId)
                .list();

        if (CollUtil.isEmpty(registrations)) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        Map<String, LocalDateTime> joinedMap = registrations.stream()
                .collect(Collectors.toMap(
                        CompetitionTeams::getCompetitionId,
                        CompetitionTeams::getJoinedAt,
                        (a, b) -> a
                ));

        List<String> competitionIds = registrations.stream()
                .map(CompetitionTeams::getCompetitionId)
                .distinct()
                .toList();

        List<CompetitionResponseVO> competitions = Optional.ofNullable(
                competitionServiceClient.getCompetitionsByIds(competitionIds).getBody()
        ).orElse(Collections.emptyList());

        Map<String, Boolean> submissionMap = submissionService.getSubmissionStatusByTeam(List.of(teamId), competitionIds);
        Map<String, BigDecimal> scoreMap = submissionService.getSubmissionScoresByTeam(List.of(teamId), competitionIds);

        List<CompetitionParticipationVO> result = competitions.stream().map(c -> {
            String key = c.getId() + ":" + teamId;
            return new CompetitionParticipationVO()
                    .setCompetitionId(c.getId())
                    .setCompetitionName(c.getName())
                    .setCategory(c.getCategory())
                    .setStatus(c.getStatus().getValue())
                    .setStartDate(c.getStartDate())
                    .setEndDate(c.getEndDate())
                    .setIsPublic(c.getIsPublic())
                    .setJoinedAt(joinedMap.get(c.getId()))
                    .setHasSubmitted(submissionMap.getOrDefault(key, false))
                    .setTotalScore(scoreMap.getOrDefault(key, null));
        }).toList();

        if (StrUtil.isNotBlank(keyword)) {
            result = result.stream().filter(vo ->
                    StrUtil.containsIgnoreCase(vo.getCompetitionName(), keyword)
                            || StrUtil.containsIgnoreCase(vo.getCategory(), keyword)
            ).toList();
        }

        Comparator<CompetitionParticipationVO> comparator = switch (sortBy) {
            case "category" -> Comparator.comparing(CompetitionParticipationVO::getCategory, String.CASE_INSENSITIVE_ORDER);
            case "startDate" -> Comparator.comparing(CompetitionParticipationVO::getStartDate);
            case "endDate" -> Comparator.comparing(CompetitionParticipationVO::getEndDate);
            case "totalScore" -> Comparator.comparing(vo -> Optional.ofNullable(vo.getTotalScore()).orElse(BigDecimal.ZERO));
            case "joinedAt" -> Comparator.comparing(CompetitionParticipationVO::getJoinedAt);
            default -> Comparator.comparing(CompetitionParticipationVO::getCompetitionName, String.CASE_INSENSITIVE_ORDER);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        result = result.stream().sorted(comparator).toList();

        int total = result.size();
        int fromIndex = Math.min((page - 1) * size, total);
        if (fromIndex >= total) {
            return new PageResponse<>(Collections.emptyList(), total, page, size, (int) Math.ceil((double) total / size));
        }

        int toIndex = Math.min(fromIndex + size, total);
        List<CompetitionParticipationVO> paged = result.subList(fromIndex, toIndex);

        return new PageResponse<>(paged, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    @Transactional
    public void cancelTeamByOrganizer(String competitionId, String teamId, String userId, String userRole) {
        if (!"ORGANIZER".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only ORGANIZER or ADMIN can remove a team's registration.");
        }

        boolean isOrganizer = competitionOrganizersService.lambdaQuery()
                .eq(CompetitionOrganizers::getCompetitionId, competitionId)
                .eq(CompetitionOrganizers::getUserId, userId)
                .exists();

        if (!isOrganizer && !"ADMIN".equalsIgnoreCase(userRole)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "You are not authorized to manage this competition.");
        }

        CompetitionTeams record = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .eq(CompetitionTeams::getTeamId, teamId)
                .one();

        if (record == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "The team is not registered for this competition.");
        }

        submissionService.lambdaUpdate()
                .eq(SubmissionRecords::getCompetitionId, competitionId)
                .eq(SubmissionRecords::getTeamId, teamId)
                .remove();

        boolean removed = competitionTeamsService.removeById(record.getId());
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove the team's registration.");
        }

        ResponseEntity<UserBriefVO> creatorResp = userServiceClient.getTeamCreator(teamId);
        UserBriefVO creator = creatorResp.getBody();
        if (creator == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team creator info not found.");
        }

        ResponseEntity<UserBriefVO> operatorResp = userServiceClient.getUserBriefById(userId);
        UserBriefVO operator = operatorResp.getBody();
        if (operator == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Operator (organizer) info not found.");
        }

        ResponseEntity<CompetitionResponseVO> compResp = competitionServiceClient.getCompetitionById(competitionId);
        CompetitionResponseVO competition = compResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition info not found.");
        }

        ParticipantRemovedMessage message = new ParticipantRemovedMessage();
        message.setUserName(creator.getName());
        message.setUserEmail(creator.getEmail());
        message.setRemovedBy(operator.getName());
        message.setCompetitionName(competition.getName());
        message.setRemovedAt(LocalDateTime.now());

        registrationNotifier.sendParticipantRemoved(message);
    }

    @Override
    public Boolean existsRegistrationByTeamId(String teamId) {
        return competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getTeamId, teamId)
                .exists();
    }

    @Override
    public RegistrationStatisticsVO getRegistrationStatistics(String competitionId) {
        ResponseEntity<CompetitionResponseVO> competitionResp = competitionServiceClient.getCompetitionById(competitionId);
        CompetitionResponseVO competition = competitionResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        int individualCount = Math.toIntExact(this.lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .count());

        int teamCount = Math.toIntExact(competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .count());

        RegistrationStatisticsVO vo = new RegistrationStatisticsVO();
        vo.setCompetitionId(competitionId);
        vo.setIndividualParticipantCount(individualCount);
        vo.setTeamParticipantCount(teamCount);
        vo.setTotalRegistrations(individualCount + teamCount);

        return vo;
    }

    @Override
    public Map<String, Map<String, Integer>> getParticipantTrend(String competitionId) {
        ResponseEntity<CompetitionResponseVO> competitionResp = competitionServiceClient.getCompetitionById(competitionId);
        CompetitionResponseVO competition = competitionResp.getBody();
        if (competition == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Competition not found");
        }

        List<CompetitionParticipants> individualRegistrations = this.lambdaQuery()
                .eq(CompetitionParticipants::getCompetitionId, competitionId)
                .list();

        List<CompetitionTeams> teamRegistrations = competitionTeamsService.lambdaQuery()
                .eq(CompetitionTeams::getCompetitionId, competitionId)
                .list();

        Map<String, Integer> individualTrend = new TreeMap<>();
        for (CompetitionParticipants participant : individualRegistrations) {
            if (participant.getCreatedAt() != null) {
                String date = participant.getCreatedAt().toLocalDate().toString();
                individualTrend.merge(date, 1, Integer::sum);
            }
        }

        Map<String, Integer> teamTrend = new TreeMap<>();
        for (CompetitionTeams team : teamRegistrations) {
            if (team.getJoinedAt() != null) {
                String date = team.getJoinedAt().toLocalDate().toString();
                teamTrend.merge(date, 1, Integer::sum);
            }
        }

        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("individual", individualTrend);
        result.put("team", teamTrend);

        return result;
    }

    @Override
    public PlatformParticipantStatisticsVO getPlatformParticipantStatistics() {
        int individualParticipants = Math.toIntExact(
                this.lambdaQuery()
                        .isNotNull(CompetitionParticipants::getUserId)
                        .count()
        );

        int teamParticipants = competitionTeamsService.countTeamParticipants();

        PlatformParticipantStatisticsVO vo = new PlatformParticipantStatisticsVO();
        vo.setIndividualParticipants(individualParticipants);
        vo.setTeamParticipants(teamParticipants);
        vo.setTotalParticipants(individualParticipants + teamParticipants);

        return vo;
    }

    @Override
    public Map<String, Map<String, Integer>> getPlatformParticipantTrend() {
        List<CompetitionParticipants> individualRegistrations = this.lambdaQuery()
                .select(CompetitionParticipants::getCreatedAt)
                .list();

        List<CompetitionTeams> teamRegistrations = competitionTeamsService.lambdaQuery()
                .select(CompetitionTeams::getJoinedAt)
                .list();

        Map<String, Integer> individualTrend = new TreeMap<>();
        for (CompetitionParticipants participant : individualRegistrations) {
            if (participant.getCreatedAt() != null) {
                String date = participant.getCreatedAt().toLocalDate().toString();
                individualTrend.merge(date, 1, Integer::sum);
            }
        }

        Map<String, Integer> teamTrend = new TreeMap<>();
        for (CompetitionTeams team : teamRegistrations) {
            if (team.getJoinedAt() != null) {
                String date = team.getJoinedAt().toLocalDate().toString();
                teamTrend.merge(date, 1, Integer::sum);
            }
        }

        Map<String, Map<String, Integer>> result = new HashMap<>();
        result.put("individual", individualTrend);
        result.put("team", teamTrend);

        return result;
    }


}
