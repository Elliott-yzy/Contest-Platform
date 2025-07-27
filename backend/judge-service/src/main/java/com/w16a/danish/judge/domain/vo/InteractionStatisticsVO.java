package com.w16a.danish.judge.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Interaction statistics for a submission (vote count + comment count).
 *
 * @author Eddy
 * @date 2025/04/21
 */
@Data
@Schema(description = "Interaction statistics for a submission")
public class InteractionStatisticsVO {

    @Schema(description = "Total number of votes")
    private Long voteCount;

    @Schema(description = "Total number of comments")
    private Long commentCount;
}
