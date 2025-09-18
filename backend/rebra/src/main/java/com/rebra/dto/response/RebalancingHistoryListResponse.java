package com.rebra.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class RebalancingHistoryListResponse {

    private List<RebalancingHistoryResponse> histories;
    private RebalancingHistorySummaryResponse summary;

    public static RebalancingHistoryListResponse of(
            List<RebalancingHistoryResponse> histories,
            RebalancingHistorySummaryResponse summary) {
        return RebalancingHistoryListResponse.builder()
                .histories(histories)
                .summary(summary)
                .build();
    }
}