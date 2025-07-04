package com.w16a.danish.judge.service;

import com.w16a.danish.judge.domain.po.SubmissionWinners;
import com.baomidou.mybatisplus.extension.service.IService;
import com.w16a.danish.judge.domain.vo.PageResponse;
import com.w16a.danish.judge.domain.vo.ScoredSubmissionVO;
import com.w16a.danish.judge.domain.vo.WinnerInfoVO;

/**
 * Service interface for managing awarded submissions.
 *
 * <p>
 * This service handles operations related to competition winners,
 * including listing scored submissions, automatically awarding winners,
 * and retrieving publicly available winner information.
 * </p>
 *
 * @author Eddy
 * @since 2025-04-18
 */
public interface ISubmissionWinnersService extends IService<SubmissionWinners> {

    /**
     * Retrieves a paginated list of scored submissions for a specific competition.
     * Supports keyword search, sorting, and pagination.
     *
     * @param userId the ID of the requesting user
     * @param userRole the role of the requesting user
     * @param competitionId the ID of the competition
     * @param keyword optional keyword for search filtering
     * @param sortBy the field to sort by
     * @param order the sort order ("asc" or "desc")
     * @param page the current page number
     * @param size the number of records per page
     * @return a paginated list of scored submissions
     */
    PageResponse<ScoredSubmissionVO> listScoredSubmissions(
            String userId,
            String userRole,
            String competitionId,
            String keyword,
            String sortBy,
            String order,
            int page,
            int size
    );

    /**
     * Automatically selects and records the winners based on submission scores
     * for a given competition. Only authorized users can perform this operation.
     *
     * @param userId the ID of the user triggering the award
     * @param userRole the role of the user triggering the award
     * @param competitionId the ID of the competition
     */
    void autoAward(String userId, String userRole, String competitionId);

    /**
     * Retrieves a paginated list of public winner information for a specific competition.
     * Intended for public display of awarded submissions.
     *
     * @param competitionId the ID of the competition
     * @param page the current page number
     * @param size the number of records per page
     * @return a paginated list of public winner details
     */
    PageResponse<WinnerInfoVO> listPublicWinners(String competitionId, int page, int size);
}
