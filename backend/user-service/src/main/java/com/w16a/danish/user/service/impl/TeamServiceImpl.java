package com.w16a.danish.user.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.w16a.danish.user.domain.dto.TeamCreateDTO;
import com.w16a.danish.user.domain.dto.TeamUpdateDTO;
import com.w16a.danish.user.domain.po.Team;
import com.w16a.danish.user.domain.po.TeamMembers;
import com.w16a.danish.user.domain.po.Users;
import com.w16a.danish.user.domain.vo.*;
import com.w16a.danish.user.exception.BusinessException;
import com.w16a.danish.user.feign.SubmissionServiceClient;
import com.w16a.danish.user.mapper.TeamMapper;
import com.w16a.danish.user.service.ITeamMembersService;
import com.w16a.danish.user.service.ITeamService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.w16a.danish.user.service.IUsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * Table for team information
 * </p>
 *
 * @author Eddy
 * @since 2025-04-16
 */
@Service
@RequiredArgsConstructor
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    private final ITeamMembersService teamMembersService;
    private final IUsersService usersService;
    private final SubmissionServiceClient submissionService;

    @Override
    @Transactional
    public TeamResponseVO createTeam(String creatorId, TeamCreateDTO dto) {
        boolean exists = this.lambdaQuery()
                .eq(Team::getName, dto.getName())
                .exists();

        if (exists) {
            throw new BusinessException(HttpStatus.CONFLICT, "Team name already exists");
        }

        String teamId = StrUtil.uuid();
        Team team = new Team()
                .setId(teamId)
                .setName(dto.getName())
                .setDescription(dto.getDescription())
                .setCreatedBy(creatorId);
        this.save(team);

        TeamMembers leader = new TeamMembers()
                .setId(StrUtil.uuid())
                .setTeamId(teamId)
                .setUserId(creatorId)
                .setRole("LEADER");
        teamMembersService.save(leader);

        UserBriefVO creator = usersService.getUserBriefById(creatorId);
        TeamMemberVO leaderVO = new TeamMemberVO();
        leaderVO.setUserId(creator.getId());
        leaderVO.setName(creator.getName());
        leaderVO.setEmail(creator.getEmail());
        leaderVO.setAvatarUrl(creator.getAvatarUrl());
        leaderVO.setDescription(creator.getDescription());
        leaderVO.setRole("LEADER");

        TeamResponseVO response = new TeamResponseVO();
        response.setId(teamId);
        response.setName(dto.getName());
        response.setDescription(dto.getDescription());
        response.setCreatedBy(creatorId);
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        response.setMembers(List.of(leaderVO));

        return response;
    }

    @Override
    @Transactional
    public void removeTeamMember(String requesterId, String teamId, String memberId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        boolean isTeamCreator = requesterId.equals(team.getCreatedBy());
        if (!isTeamCreator) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the team creator can remove members");
        }

        if (requesterId.equals(memberId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "You cannot remove yourself as the team creator");
        }

        boolean exists = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, memberId)
                .exists();
        if (!exists) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "The member is not part of this team");
        }

        boolean removed = teamMembersService.lambdaUpdate()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, memberId)
                .remove();
        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove the member from the team");
        }
    }

    @Override
    @Transactional
    public void deleteTeam(String userId, String userRole, String teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found.");
        }

        boolean isCreator = userId.equals(team.getCreatedBy());
        boolean isAdmin = "ADMIN".equalsIgnoreCase(userRole);
        if (!isCreator && !isAdmin) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the team creator or an ADMIN can delete this team.");
        }

        Boolean hasSubmission = Optional.ofNullable(submissionService.existsByTeamId(teamId).getBody())
                .orElse(false);
        if (hasSubmission) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot delete a team that has submitted work.");
        }

        Boolean hasRegistration = Optional.ofNullable(submissionService.existsRegistrationByTeamId(teamId).getBody())
                .orElse(false);
        if (hasRegistration) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Cannot delete a team that has registered for competitions.");
        }

        boolean membersRemoved = teamMembersService.lambdaUpdate()
                .eq(TeamMembers::getTeamId, teamId)
                .remove();
        if (!membersRemoved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove team members.");
        }

        boolean deleted = this.removeById(teamId);
        if (!deleted) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete the team.");
        }
    }

    @Override
    @Transactional
    public void updateTeam(String userId, String teamId, TeamUpdateDTO dto) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        if (!userId.equals(team.getCreatedBy())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "Only the team creator can update the team");
        }

        if (!dto.getName().equals(team.getName())) {
            boolean nameExists = this.lambdaQuery()
                    .eq(Team::getName, dto.getName())
                    .ne(Team::getId, teamId)
                    .exists();
            if (nameExists) {
                throw new BusinessException(HttpStatus.CONFLICT, "Team name already exists");
            }
        }

        team.setName(dto.getName());
        team.setDescription(dto.getDescription());
        team.setUpdatedAt(null);

        boolean updated = this.updateById(team);
        if (!updated) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update team info");
        }
    }

    @Override
    @Transactional
    public void joinTeam(String teamId, String userId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        boolean alreadyJoined = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, userId)
                .exists();

        if (alreadyJoined) {
            throw new BusinessException(HttpStatus.CONFLICT, "You are already in this team");
        }

        TeamMembers member = new TeamMembers()
                .setId(StrUtil.uuid())
                .setTeamId(teamId)
                .setUserId(userId)
                .setRole("MEMBER");

        boolean saved = teamMembersService.save(member);
        if (!saved) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to join the team");
        }
    }

    @Override
    @Transactional
    public void leaveTeam(String teamId, String userId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        if (userId.equals(team.getCreatedBy())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Team creator cannot leave the team");
        }

        boolean isMember = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, userId)
                .exists();

        if (!isMember) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "You are not a member of this team");
        }

        boolean removed = teamMembersService.lambdaUpdate()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, userId)
                .remove();

        if (!removed) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to leave the team");
        }
    }

    @Override
    public TeamResponseVO getTeamResponseById(String teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        List<TeamMembers> members = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .list();

        List<String> userIds = members.stream()
                .map(TeamMembers::getUserId)
                .toList();

        List<UserBriefVO> userInfos = usersService.getUsersByIds(userIds, null);
        if (userInfos == null) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to fetch team member info");
        }

        List<TeamMemberVO> memberVOList = members.stream()
                .map(member -> {
                    UserBriefVO user = userInfos.stream()
                            .filter(u -> u.getId().equals(member.getUserId()))
                            .findFirst()
                            .orElse(null);

                    if (user == null) {
                        return null;
                    }

                    TeamMemberVO vo = new TeamMemberVO();
                    vo.setUserId(user.getId());
                    vo.setName(user.getName());
                    vo.setEmail(user.getEmail());
                    vo.setAvatarUrl(user.getAvatarUrl());
                    vo.setDescription(user.getDescription());
                    vo.setRole(member.getRole());
                    return vo;
                })
                .filter(Objects::nonNull)
                .toList();

        TeamResponseVO vo = new TeamResponseVO();
        vo.setId(team.getId());
        vo.setName(team.getName());
        vo.setDescription(team.getDescription());
        vo.setCreatedBy(team.getCreatedBy());
        vo.setCreatedAt(team.getCreatedAt());
        vo.setUpdatedAt(team.getUpdatedAt());
        vo.setMembers(memberVOList);

        return vo;
    }

    @Override
    public PageResponse<TeamSummaryVO> getTeamsCreatedBy(String userId, int page, int size, String sortBy, String order, String keyword) {
        List<Team> all = this.lambdaQuery()
                .eq(Team::getCreatedBy, userId)
                .list();

        if (CollUtil.isEmpty(all)) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        if (StrUtil.isNotBlank(keyword)) {
            all = all.stream()
                    .filter(team -> StrUtil.containsIgnoreCase(team.getName(), keyword)
                            || StrUtil.containsIgnoreCase(team.getDescription(), keyword))
                    .toList();
        }

        String creatorName = Optional.ofNullable(usersService.getUserBriefById(userId))
                .map(UserBriefVO::getName)
                .orElse("Unknown");

        Map<String, Long> memberCountMap = teamMembersService.lambdaQuery()
                .in(TeamMembers::getTeamId, all.stream().map(Team::getId).toList())
                .list()
                .stream()
                .collect(Collectors.groupingBy(TeamMembers::getTeamId, Collectors.counting()));

        Comparator<Team> comparator = switch (sortBy) {
            case "name" -> Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER);
            case "createdAt" -> Comparator.comparing(Team::getCreatedAt);
            default -> Comparator.comparing(Team::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        all = all.stream().sorted(comparator).toList();

        int total = all.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<TeamSummaryVO> pagedList = all.subList(fromIndex, toIndex).stream()
                .map(team -> {
                    TeamSummaryVO vo = new TeamSummaryVO();
                    vo.setId(team.getId());
                    vo.setName(team.getName());
                    vo.setDescription(team.getDescription());
                    vo.setCreatedAt(team.getCreatedAt());
                    vo.setLeaderName(creatorName);
                    vo.setMemberCount(memberCountMap.getOrDefault(team.getId(), 0L).intValue());
                    return vo;
                }).toList();

        return new PageResponse<>(pagedList, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<TeamSummaryVO> getTeamsJoinedBy(String userId, int page, int size, String sortBy, String order, String keyword) {
        // 1. Fetch all teams the user has joined
        List<TeamMembers> memberships = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getUserId, userId)
                .list();

        if (memberships.isEmpty()) {
            return new PageResponse<>(Collections.emptyList(), 0, page, size, 0);
        }

        // 2. Prepare teamId -> joinedAt map for sorting
        Map<String, LocalDateTime> joinedAtMap = memberships.stream()
                .collect(Collectors.toMap(TeamMembers::getTeamId, TeamMembers::getJoinedAt));

        List<String> teamIds = new ArrayList<>(joinedAtMap.keySet());

        // 3. Fetch team records
        List<Team> teams = this.lambdaQuery().in(Team::getId, teamIds).list();

        // 4. Apply keyword search (name or description)
        if (StrUtil.isNotBlank(keyword)) {
            teams = teams.stream()
                    .filter(t -> StrUtil.containsIgnoreCase(t.getName(), keyword)
                            || StrUtil.containsIgnoreCase(t.getDescription(), keyword))
                    .toList();
        }

        // 5. Fetch leader name and member counts
        Map<String, String> leaderNameMap = usersService.getUsersByIds(
                teams.stream().map(Team::getCreatedBy).distinct().toList(), null
        ).stream().collect(Collectors.toMap(UserBriefVO::getId, UserBriefVO::getName));

        Map<String, Long> memberCountMap = teamMembersService.lambdaQuery()
                .in(TeamMembers::getTeamId, teamIds)
                .list()
                .stream()
                .collect(Collectors.groupingBy(TeamMembers::getTeamId, Collectors.counting()));

        // 6. Sorting
        Comparator<Team> comparator = switch (sortBy) {
            case "name" -> Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER);
            case "updatedAt" -> Comparator.comparing(Team::getUpdatedAt);
            case "joinedAt" -> Comparator.comparing(t -> joinedAtMap.getOrDefault(t.getId(), t.getCreatedAt()));
            default -> Comparator.comparing(Team::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        teams = teams.stream().sorted(comparator).toList();

        // 7. Pagination
        int total = teams.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<TeamSummaryVO> paged = teams.subList(fromIndex, toIndex).stream()
                .map(t -> {
                    TeamSummaryVO vo = new TeamSummaryVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setDescription(t.getDescription());
                    vo.setCreatedAt(joinedAtMap.getOrDefault(t.getId(), t.getCreatedAt()));
                    vo.setLeaderName(leaderNameMap.getOrDefault(t.getCreatedBy(), "Unknown"));
                    vo.setMemberCount(memberCountMap.getOrDefault(t.getId(), 0L).intValue());
                    return vo;
                }).toList();

        return new PageResponse<>(paged, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public PageResponse<TeamSummaryVO> getAllTeams(int page, int size, String sortBy, String order, String keyword) {
        List<Team> all = this.lambdaQuery().list();

        if (StrUtil.isNotBlank(keyword)) {
            all = all.stream()
                    .filter(t -> StrUtil.containsIgnoreCase(t.getName(), keyword)
                            || StrUtil.containsIgnoreCase(t.getDescription(), keyword))
                    .toList();
        }

        Map<String, String> leaderNameMap = usersService.getUsersByIds(
                all.stream().map(Team::getCreatedBy).distinct().toList(), null
        ).stream().collect(Collectors.toMap(UserBriefVO::getId, UserBriefVO::getName));

        Map<String, Long> memberCountMap = teamMembersService.lambdaQuery()
                .in(TeamMembers::getTeamId, all.stream().map(Team::getId).toList())
                .list()
                .stream()
                .collect(Collectors.groupingBy(TeamMembers::getTeamId, Collectors.counting()));

        Comparator<Team> comparator = switch (sortBy) {
            case "name" -> Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER);
            case "updatedAt" -> Comparator.comparing(Team::getUpdatedAt);
            case "createdAt" -> Comparator.comparing(Team::getCreatedAt);
            default -> Comparator.comparing(Team::getCreatedAt);
        };
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }

        all = all.stream().sorted(comparator).toList();

        int total = all.size();
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<TeamSummaryVO> paged = all.subList(fromIndex, toIndex).stream()
                .map(t -> {
                    TeamSummaryVO vo = new TeamSummaryVO();
                    vo.setId(t.getId());
                    vo.setName(t.getName());
                    vo.setDescription(t.getDescription());
                    vo.setCreatedAt(t.getCreatedAt());
                    vo.setLeaderName(leaderNameMap.getOrDefault(t.getCreatedBy(), "Unknown"));
                    vo.setMemberCount(memberCountMap.getOrDefault(t.getId(), 0L).intValue());
                    return vo;
                })
                .toList();

        return new PageResponse<>(paged, total, page, size, (int) Math.ceil((double) total / size));
    }

    @Override
    public UserBriefVO getTeamCreator(String teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        Users creator = usersService.getById(team.getCreatedBy());
        if (creator == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team creator not found");
        }

        return UserBriefVO.builder()
                .id(creator.getId())
                .name(creator.getName())
                .email(creator.getEmail())
                .avatarUrl(creator.getAvatarUrl())
                .description(creator.getDescription())
                .createdAt(creator.getCreatedAt())
                .build();
    }

    @Override
    public List<TeamInfoVO> getTeamBriefByIds(List<String> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Team> teams = this.lambdaQuery()
                .in(Team::getId, teamIds)
                .list();

        return teams.stream().map(t -> {
            TeamInfoVO vo = new TeamInfoVO();
            vo.setTeamId(t.getId());
            vo.setTeamName(t.getName());
            vo.setDescription(t.getDescription());
            vo.setCreatedAt(t.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    public boolean isUserInTeam(String userId, String teamId) {
        return teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .eq(TeamMembers::getUserId, userId)
                .exists();
    }

    @Override
    public List<UserBriefVO> getTeamMembers(String teamId) {
        Team team = this.getById(teamId);
        if (team == null) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "Team not found");
        }

        List<TeamMembers> members = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getTeamId, teamId)
                .list();

        if (members.isEmpty()) {
            return List.of();
        }

        List<String> userIds = members.stream()
                .map(TeamMembers::getUserId)
                .distinct()
                .toList();

        List<UserBriefVO> userInfos = usersService.getUsersByIds(userIds, null);
        if (userInfos == null || userInfos.isEmpty()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "Failed to fetch team member info");
        }

        return userInfos;
    }

    @Override
    public List<String> getAllJoinedTeamIdsByUser(String userId) {
        List<TeamMembers> memberships = teamMembersService.lambdaQuery()
                .eq(TeamMembers::getUserId, userId)
                .list();

        if (CollUtil.isEmpty(memberships)) {
            return Collections.emptyList();
        }

        return memberships.stream()
                .map(TeamMembers::getTeamId)
                .distinct()
                .toList();
    }

}
