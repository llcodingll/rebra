package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false, unique = true)
    private Account account;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "safe_asset_ratio")
    private BigDecimal safeAssetRatio;

    @Column(name = "rebalancing_strategy")
    @Enumerated(EnumType.STRING)
    private RebalancingStrategy rebalancingStrategy;

    @Column(name = "auto_rebalancing")
    private Boolean autoRebalancing = false;

    @Column(name = "rebalancing_period")
    @Enumerated(EnumType.STRING)
    private RebalancingPeriod rebalancingPeriod;

    @Column(name = "rebalancing_interval")
    private Integer rebalancingInterval;

    @Column(name = "rebalancing_start_date")
    private LocalDate rebalancingStartDate;

    @Column(name = "last_rebalance_date")
    private LocalDate lastRebalanceDate;

    @Column(name = "next_rebalance_date")
    private LocalDate nextRebalanceDate;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PortfolioStock> portfolioStocks = new ArrayList<>();

    @Builder
    public Portfolio(User user, Account account, String name, String description) {
        this.user = user;
        this.account = account;
        this.name = name;
        this.description = description;
        // 나머지 필드들은 null 또는 기본값으로 초기화
        this.autoRebalancing = false; // 기본값 false
    }

    public void updatePortfolio(String name, String description, BigDecimal safeAssetRatio,
                               RebalancingStrategy rebalancingStrategy, Boolean autoRebalancing,
                               RebalancingPeriod rebalancingPeriod, Integer rebalancingInterval,
                               LocalDate rebalancingStartDate) {
        this.name = name;
        this.description = description;
        this.safeAssetRatio = safeAssetRatio;
        this.rebalancingStrategy = rebalancingStrategy;
        this.autoRebalancing = autoRebalancing;
        this.rebalancingPeriod = rebalancingPeriod;
        this.rebalancingInterval = rebalancingInterval;
        this.rebalancingStartDate = rebalancingStartDate;
    }

    public void updateAutoRebalancing(Boolean autoRebalancing) {
        this.autoRebalancing = autoRebalancing;
    }

    public void updateRebalancingSettings(LocalDate rebalancingStartDate,
                                         RebalancingPeriod rebalancingPeriod,
                                         Integer rebalancingInterval) {
        this.rebalancingStartDate = rebalancingStartDate;
        this.rebalancingPeriod = rebalancingPeriod;
        this.rebalancingInterval = rebalancingInterval;
    }

    public void updateBasicInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void updateNextRebalanceDate(LocalDate nextRebalanceDate) {
        this.nextRebalanceDate = nextRebalanceDate;
    }

    public void updateLastRebalanceDate(LocalDate lastRebalanceDate) {
        this.lastRebalanceDate = lastRebalanceDate;
    }

    // 연관관계 편의 메서드
    public void addPortfolioStock(PortfolioStock portfolioStock) {
        this.portfolioStocks.add(portfolioStock);
    }

    public void removePortfolioStock(PortfolioStock portfolioStock) {
        this.portfolioStocks.remove(portfolioStock);
    }
}