package com.rebra.dto.request;

import com.rebra.entity.RebalancingPeriod;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PortfolioRebalancingSettingsRequest {

    private LocalDate rebalancingStartDate;
    private RebalancingPeriod rebalancingPeriod;
    private Integer rebalancingInterval;
}