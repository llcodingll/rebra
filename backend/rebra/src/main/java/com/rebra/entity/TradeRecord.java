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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "trade_record")
public class TradeRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rebalancing_order_id", nullable = false)
    private RebalancingOrder rebalancingOrder;

    @Column(name = "stock_code", nullable = false)
    private String stockCode;

    @Column(name = "stock_name", nullable = false)
    private String stockName;

    @Column(name = "trade_type", nullable = false)
    private String tradeType;

    @Column(name = "trade_date", nullable = false)
    private LocalDateTime tradeDate;

    @Column(name = "executed_shares", nullable = false)
    private Integer executedShares;

    @Column(name = "executed_price", nullable = false)
    private BigDecimal executedPrice;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private String status;

    @Column(name = "order_number", nullable = false)
    private String orderNumber;

    @Builder
    public TradeRecord(RebalancingOrder rebalancingOrder, String stockCode, String stockName,
                       String tradeType, LocalDateTime tradeDate, Integer executedShares,
                       BigDecimal executedPrice, BigDecimal totalAmount, BigDecimal fee,
                       String status, String orderNumber) {
        this.rebalancingOrder = rebalancingOrder;
        this.stockCode = stockCode;
        this.stockName = stockName;
        this.tradeType = tradeType;
        this.tradeDate = tradeDate;
        this.executedShares = executedShares;
        this.executedPrice = executedPrice;
        this.totalAmount = totalAmount;
        this.fee = fee;
        this.status = status;
        this.orderNumber = orderNumber;
    }
}