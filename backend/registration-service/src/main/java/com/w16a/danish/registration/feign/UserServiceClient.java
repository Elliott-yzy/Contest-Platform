package com.w16a.danish.registration.feign;

import com.w16a.danish.registration.domain.vo.TeamInfoVO;
import com.w16a.danish.registration.domain.vo.UserBriefVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign client for communicating with user-service.
 * Used to fetch user brief info by IDs and roles.
 *
 * @author Eddy
 * @since 2025/04/04
 */
@FeignClient(name = "user-service")
public interface UserServiceClient {

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
     * Fetch the creator info of the team (used for validating registration authority).
     */
    @GetMapping("/teams/{teamId}/creator")
    ResponseEntity<UserBriefVO> getTeamCreator(@PathVariable("teamId") String teamId);

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
     * Fetch brief info (id, name, description, createdAt) for a single team.
     * This is a public endpoint used by registration service.
     *
     * @param teamId team ID to query
     * @return team brief info
     */
    @GetMapping("/teams/public/is-member")
    ResponseEntity<Boolean> isUserInTeam(
            @RequestParam("userId") String userId,
            @RequestParam("teamId") String teamId
    );
}
