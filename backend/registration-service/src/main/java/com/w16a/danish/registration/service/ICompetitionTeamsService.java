package com.w16a.danish.registration.service;

import com.w16a.danish.registration.domain.po.CompetitionTeams;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * Mapping between teams and competitions
 * </p>
 *
 * @author Eddy
 * @since 2025-04-17
 */
public interface ICompetitionTeamsService extends IService<CompetitionTeams> {

    int countTeamParticipants();
}
