package com.rebra.dto.response;

import com.rebra.entity.RebalancingOrder;
import com.rebra.enums.ExecutionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class RebalancingHistoryDetailResponse {

    private Long orderId;
    private ExecutionType executionType;
    private LocalDateTime executedAt;
    private Double cumulativeReturn; // 누적 수익률
    private Long totalPortfolioValue; // 포트폴리오 총 평가액
    private List<TradeDetailResponse> trades;

    public static RebalancingHistoryDetailResponse from(RebalancingOrder order, List<TradeDetailResponse> trades) {
        return RebalancingHistoryDetailResponse.builder()
                .orderId(order.getId())
                .executionType(order.getExecutionType())
                .executedAt(order.getRebalancingDate())
                .cumulativeReturn(order.getCumulativeReturn())
                .totalPortfolioValue(order.getTotalPortfolioValue())
                .trades(trades)
                .build();
    }

    // 더미 데이터 생성을 위한 정적 메서드 (포트폴리오 시작점용)
    public static RebalancingHistoryDetailResponse createStartingPoint(LocalDateTime createdAt) {
        return RebalancingHistoryDetailResponse.builder()
                .orderId(null) // 더미 데이터이므로 null
                .executionType(null) // 시작점은 ExecutionType 없음
                .executedAt(createdAt)
                .cumulativeReturn(1.0) // 100% 기준
                .totalPortfolioValue(10000000L) // 초기 1000만원 기준
                .trades(List.of()) // 빈 리스트
                .build();
    }
}