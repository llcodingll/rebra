package com.rebra.dto.response;

import com.rebra.entity.BacktestRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class BacktestListResponse {

    private Long id;
    private String testName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BacktestRecord.RebalancingType rebalancingType;
    private BacktestRecord.RebalancingPeriod rebalancingPeriod;
    private BacktestRecord.BacktestStatus status;
    private LocalDateTime createdAt;
    private String errorMessage;

    public static BacktestListResponse from(BacktestRecord record) {
        return BacktestListResponse.builder()
                .id(record.getId())
                .testName(record.getTestName())
                .startDate(record.getStartDate())
                .endDate(record.getEndDate())
                .rebalancingType(record.getRebalancingType())
                .rebalancingPeriod(record.getRebalancingPeriod())
                .status(record.getStatus())
                .createdAt(record.getCreatedAt())
                .errorMessage(record.getErrorMessage())
                .build();
    }

    // 진행 상태 표시용
    public String getStatusDisplay() {
        return switch (status) {
            case PENDING -> "대기 중";
            case PROCESSING -> "계산 중";
            case COMPLETED -> "완료";
            case FAILED -> "실패";
        };
    }

    // 기간 정보
    public long getPeriodDays() {
        return startDate.until(endDate).getDays() + 1;
    }

}