package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "portfolio")
public class Portfolio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "safe_asset_ratio", nullable = false)
    private BigDecimal safeAssetRatio;

    @Column(name = "risky_asset_ratio", nullable = false)
    private BigDecimal riskyAssetRatio;

    @Column(name = "rebalancing_strategy", nullable = false)
    private String rebalancingStrategy;

    @Column(name = "rebalancing_period", nullable = false)
    private String rebalancingPeriod;

    @Column(name = "threshold_percentage", nullable = false)
    private BigDecimal thresholdPercentage;

    @Column(name = "last_rebalance_date")
    private LocalDate lastRebalanceDate;

    @Column(name = "next_rebalance_date")
    private LocalDate nextRebalanceDate;

    @Builder
    public Portfolio(User user, Account account, String name, String description,
                     BigDecimal safeAssetRatio, BigDecimal riskyAssetRatio,
                     String rebalancingStrategy, String rebalancingPeriod,
                     BigDecimal thresholdPercentage, LocalDate lastRebalanceDate,
                     LocalDate nextRebalanceDate) {
        this.user = user;
        this.account = account;
        this.name = name;
        this.description = description;
        this.safeAssetRatio = safeAssetRatio;
        this.riskyAssetRatio = riskyAssetRatio;
        this.rebalancingStrategy = rebalancingStrategy;
        this.rebalancingPeriod = rebalancingPeriod;
        this.thresholdPercentage = thresholdPercentage;
        this.lastRebalanceDate = lastRebalanceDate;
        this.nextRebalanceDate = nextRebalanceDate;
    }
}