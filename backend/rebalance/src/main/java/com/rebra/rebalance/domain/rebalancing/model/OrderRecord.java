package com.rebra.rebalance.domain.rebalancing.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(schema = "rebalance", name = "order_record")
public class OrderRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "execution_id", nullable = false)
    private RebalancingExecution execution;

    @Column(nullable = false)
    private String stockCode;

    private String stockName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Column(nullable = false)
    private Integer quantity;

    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    private String kisOrderNumber;

    private String kisOrderDate;

    private String failReason;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static OrderRecord pending(RebalancingExecution execution,
                                      String stockCode, String stockName,
                                      OrderType orderType, int quantity, long price) {
        OrderRecord record = new OrderRecord();
        record.execution = execution;
        record.stockCode = stockCode;
        record.stockName = stockName;
        record.orderType = orderType;
        record.quantity = quantity;
        record.price = price;
        record.status = OrderStatus.PENDING;
        return record;
    }

    public void complete(String kisOrderNumber, String kisOrderDate) {
        this.status = OrderStatus.COMPLETED;
        this.kisOrderNumber = kisOrderNumber;
        this.kisOrderDate = kisOrderDate;
    }

    public void fail(String reason) {
        this.status = OrderStatus.FAILED;
        this.failReason = reason;
    }
}
