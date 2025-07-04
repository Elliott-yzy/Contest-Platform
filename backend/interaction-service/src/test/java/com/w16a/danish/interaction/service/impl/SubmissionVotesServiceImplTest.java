package com.w16a.danish.interaction.service.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.w16a.danish.interaction.domain.po.SubmissionVotes;
import com.w16a.danish.interaction.exception.BusinessException;
import com.w16a.danish.interaction.mapper.SubmissionVotesMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SubmissionVotesServiceImplTest {

    @Spy
    @InjectMocks
    private SubmissionVotesServiceImpl submissionVotesService;

    @Mock
    private SubmissionVotesMapper submissionVotesMapper;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        var baseMapperField = SubmissionVotesServiceImpl.class.getSuperclass().getDeclaredField("baseMapper");
        baseMapperField.setAccessible(true);
        baseMapperField.set(submissionVotesService, submissionVotesMapper);

        LambdaQueryChainWrapper<SubmissionVotes> query = mock(LambdaQueryChainWrapper.class);
        doReturn(query).when(submissionVotesService).lambdaQuery();
        when(query.eq(any(), any())).thenReturn(query);

        LambdaUpdateChainWrapper<SubmissionVotes> update = mock(LambdaUpdateChainWrapper.class);
        doReturn(update).when(submissionVotesService).lambdaUpdate();
        when(update.eq(any(), any())).thenReturn(update);
    }

    @Test
    @DisplayName("✅ Vote success when not already voted")
    void testVoteSuccess() {
        when(submissionVotesService.lambdaQuery().exists()).thenReturn(false);
        when(submissionVotesMapper.insert(any(SubmissionVotes.class))).thenReturn(1);

        submissionVotesService.vote("submissionId", "userId");

        verify(submissionVotesMapper, times(1)).insert(any(SubmissionVotes.class));
    }

    @Test
    @DisplayName("❌ Vote fails when already voted")
    void testVoteAlreadyVoted() {
        when(submissionVotesService.lambdaQuery().exists()).thenReturn(true);

        assertThatThrownBy(() -> submissionVotesService.vote("submissionId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("already voted");
    }

    @Test
    @DisplayName("✅ Unvote success when already voted")
    void testUnvoteSuccess() {
        when(submissionVotesService.lambdaQuery().exists()).thenReturn(true);
        when(submissionVotesService.lambdaUpdate().remove()).thenReturn(true);

        submissionVotesService.unvote("submissionId", "userId");

        verify(submissionVotesService.lambdaUpdate(), times(1)).remove();
    }

    @Test
    @DisplayName("❌ Unvote fails when no vote exists")
    void testUnvoteNotVoted() {
        when(submissionVotesService.lambdaQuery().exists()).thenReturn(false);

        assertThatThrownBy(() -> submissionVotesService.unvote("submissionId", "userId"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not voted");
    }

    @Test
    @DisplayName("✅ Count votes by submissionId")
    void testCountVotes() {
        when(submissionVotesService.lambdaQuery().count()).thenReturn(5L);

        long count = submissionVotesService.countVotes("submissionId");

        assertThat(count).isEqualTo(5L);
    }

    @Test
    @DisplayName("✅ Check has voted returns true")
    void testHasVotedTrue() {
        when(submissionVotesService.lambdaQuery().exists()).thenReturn(true);

        boolean result = submissionVotesService.hasVoted("submissionId", "userId");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("✅ Count all votes across all submissions")
    void testCountAllVotes() {
        when(submissionVotesService.lambdaQuery().count()).thenReturn(10L);

        long totalVotes = submissionVotesService.countAllVotes();

        assertThat(totalVotes).isEqualTo(10L);
    }
}
