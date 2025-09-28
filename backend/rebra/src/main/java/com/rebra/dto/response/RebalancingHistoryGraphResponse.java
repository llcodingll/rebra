package com.rebra.dto.response;

import com.rebra.entity.RebalancingOrder;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RebalancingHistoryGraphResponse {

    private Long orderId;
    private LocalDateTime executedAt;
    private Double cumulativeReturn; // 누적 수익률

    public static RebalancingHistoryGraphResponse from(RebalancingOrder order) {
        return RebalancingHistoryGraphResponse.builder()
                .orderId(order.getId())
                .executedAt(order.getRebalancingDate())
                .cumulativeReturn(order.getCumulativeReturn())
                .build();
    }

    // 더미 데이터 생성을 위한 정적 메서드 (포트폴리오 시작점용)
    public static RebalancingHistoryGraphResponse createStartingPoint(LocalDateTime createdAt) {
        return RebalancingHistoryGraphResponse.builder()
                .orderId(null) // 더미 데이터이므로 null
                .executedAt(createdAt)
                .cumulativeReturn(1.0) // 100% 기준
                .build();
    }
}