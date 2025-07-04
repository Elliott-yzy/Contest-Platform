package com.w16a.danish.judge.feign;

import com.w16a.danish.judge.domain.vo.TeamInfoVO;
import com.w16a.danish.judge.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for communicating with user-service.
 * Used to fetch user brief info by IDs, emails, and roles.
 *
 * @author Eddy
 * @since 2025/04/04
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PostMapping("/users/query-by-emails")
    ResponseEntity<List<UserBriefVO>> getUsersByEmails(
            @RequestBody List<String> emails
    );

    @PostMapping("/users/query-by-ids")
    ResponseEntity<List<UserBriefVO>> getUsersByIds(
            @RequestBody List<String> userIds,
            @RequestParam(required = false) String role
    );

    /**
     * Query a single user by user ID.
     */
    @GetMapping("/users/{userId}")
    ResponseEntity<UserBriefVO> getUserBriefById(@PathVariable("userId") String userId);

    /**
     * Query all members of a team by team ID.
     *
     * @param teamId ID of the team
     * @return List of UserBriefVO (basic user info)
     */
    @GetMapping("/teams/public/{teamId}/members")
    ResponseEntity<List<UserBriefVO>> getTeamMembersByTeamId(@PathVariable("teamId") String teamId);

    /**
     * Fetch brief info (id, name, description, createdAt) for multiple teams.
     * This is a public endpoint used by registration service.
     *
     * @param teamIds list of team IDs to query
     * @return list of team brief info
     */
    @PostMapping("/teams/public/brief")
    ResponseEntity<List<TeamInfoVO>> getTeamBriefByIds(@RequestBody List<String> teamIds);

    /**
     * Query all team IDs that a user has joined.
     *
     * @param userId ID of the user
     * @return List of team IDs
     */
    @GetMapping("/teams/public/joined")
    ResponseEntity<List<String>> getJoinedTeamIdsByUser(
            @RequestParam("userId") String userId
    );
}