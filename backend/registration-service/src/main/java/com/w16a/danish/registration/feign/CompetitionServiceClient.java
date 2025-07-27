package com.w16a.danish.registration.feign;

import com.w16a.danish.registration.domain.vo.CompetitionResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Feign client for communicating with competition-service.
 * Used to fetch competition details from registration-service.
 *
 * @author Eddy ZHANG
 * @date 2025/04/03
 */
@FeignClient(name = "competition-service", path = "/competitions")
public interface CompetitionServiceClient {

    /**
     * Get competition details by ID.
     *
     * @param id competition ID
     * @return competition detail response
     */
    @GetMapping("/{id}")
    ResponseEntity<CompetitionResponseVO> getCompetitionById(@PathVariable("id") String id);

    /**
     *
     * Get competition details by a list of IDs.
     *
     * @param ids list of competition IDs
     * @return {@link ResponseEntity }<{@link List }<{@link CompetitionResponseVO }>>
     */
    @PostMapping("/batch/ids")
    ResponseEntity<List<CompetitionResponseVO>> getCompetitionsByIds(@RequestBody List<String> ids);
}
