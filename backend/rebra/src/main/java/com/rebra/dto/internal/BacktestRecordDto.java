package com.rebra.dto.internal;

import com.rebra.entity.BacktestRecord;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BacktestRecordDto {
    private Long id;
    private String testName;
    private LocalDate startDate;
    private LocalDate endDate;
    private BacktestRecord.RebalancingType rebalancingType;
    private BacktestRecord.RebalancingPeriod rebalancingPeriod;
    private BacktestRecord.BacktestStatus status;
    
    public static BacktestRecordDto from(BacktestRecord record) {
        return BacktestRecordDto.builder()
            .id(record.getId())
            .testName(record.getTestName())
            .startDate(record.getStartDate())
            .endDate(record.getEndDate())
            .rebalancingType(record.getRebalancingType())
            .rebalancingPeriod(record.getRebalancingPeriod())
            .status(record.getStatus())
            .build();
    }
}