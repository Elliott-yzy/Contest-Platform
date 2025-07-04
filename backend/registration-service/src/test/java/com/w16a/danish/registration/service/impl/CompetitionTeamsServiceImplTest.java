package com.w16a.danish.registration.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.w16a.danish.registration.domain.po.CompetitionTeams;
import com.w16a.danish.registration.mapper.CompetitionTeamsMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for CompetitionTeamsServiceImpl
 */
class CompetitionTeamsServiceImplTest {

    @InjectMocks
    @Spy
    private CompetitionTeamsServiceImpl competitionTeamsService;

    @Mock
    private CompetitionTeamsMapper competitionTeamsMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Mock lambdaQuery
        LambdaQueryChainWrapper<CompetitionTeams> lambdaQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(lambdaQuery).when(competitionTeamsService).lambdaQuery();
        when(lambdaQuery.count()).thenReturn(5L); // Mock default behavior
    }

    @Test
    @DisplayName("✅ Should return correct team participants count")
    void testCountTeamParticipants_Success() {
        // Arrange
        LambdaQueryChainWrapper<CompetitionTeams> lambdaQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(lambdaQuery).when(competitionTeamsService).lambdaQuery();
        when(lambdaQuery.count()).thenReturn(8L); // Suppose we have 8 team participants

        // Act
        int count = competitionTeamsService.countTeamParticipants();

        // Assert
        assertThat(count).isEqualTo(8);
    }

    @Test
    @DisplayName("✅ Should return 0 if no team participants exist")
    void testCountTeamParticipants_Zero() {
        // Arrange
        LambdaQueryChainWrapper<CompetitionTeams> lambdaQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(lambdaQuery).when(competitionTeamsService).lambdaQuery();
        when(lambdaQuery.count()).thenReturn(0L); // No teams

        // Act
        int count = competitionTeamsService.countTeamParticipants();

        // Assert
        assertThat(count).isZero();
    }

    @Test
    @DisplayName("❌ Should throw RuntimeException if count query fails")
    void testCountTeamParticipants_ThrowsException() {
        // Arrange
        LambdaQueryChainWrapper<CompetitionTeams> lambdaQuery = mock(LambdaQueryChainWrapper.class);
        doReturn(lambdaQuery).when(competitionTeamsService).lambdaQuery();
        when(lambdaQuery.count()).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatThrownBy(() -> competitionTeamsService.countTeamParticipants())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");
    }
}
