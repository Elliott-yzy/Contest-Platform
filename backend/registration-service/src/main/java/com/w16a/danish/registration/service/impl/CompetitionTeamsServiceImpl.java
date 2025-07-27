package com.w16a.danish.registration.service.impl;

import com.w16a.danish.registration.domain.po.CompetitionTeams;
import com.w16a.danish.registration.mapper.CompetitionTeamsMapper;
import com.w16a.danish.registration.service.ICompetitionTeamsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Mapping between teams and competitions
 * </p>
 *
 * @author Eddy
 * @since 2025-04-17
 */
@Service
public class CompetitionTeamsServiceImpl extends ServiceImpl<CompetitionTeamsMapper, CompetitionTeams> implements ICompetitionTeamsService {

    @Override
    public int countTeamParticipants() {
        return Math.toIntExact(this.lambdaQuery().count());
    }
}
