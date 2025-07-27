package com.w16a.danish.interaction.service;

import com.w16a.danish.interaction.domain.po.SubmissionVotes;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 *
 * SubmissionVotesService interface for handling submission votes.
 *
 * @author Eddy ZHANG
 * @date 2025/04/08
 */
public interface ISubmissionVotesService extends IService<SubmissionVotes> {

    void vote(String submissionId, String userId);

    void unvote(String submissionId, String userId);

    long countVotes(String submissionId);

    boolean hasVoted(String submissionId, String userId);

    Long countAllVotes();

}
