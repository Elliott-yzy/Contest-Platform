package com.w16a.danish.registration.service.impl;

import com.w16a.danish.registration.domain.po.CompetitionOrganizers;
import com.w16a.danish.registration.mapper.CompetitionOrganizersMapper;
import com.w16a.danish.registration.service.ICompetitionOrganizersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 *
 * This class handles the management of competition organizers.
 *
 * @author Eddy ZHANG
 * @date 2025/04/03
 */
@Service
@RequiredArgsConstructor
public class CompetitionOrganizersServiceImpl extends ServiceImpl<CompetitionOrganizersMapper, CompetitionOrganizers> implements ICompetitionOrganizersService {

}
