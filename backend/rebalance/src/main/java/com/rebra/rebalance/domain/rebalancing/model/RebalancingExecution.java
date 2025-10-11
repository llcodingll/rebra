package com.rebra.rebalance.domain.rebalancing.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(schema = "rebalance", name = "rebalancing_execution")
public class RebalancingExecution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private Long jobId;         // rebalancing_order.id (멱등성 키)

    @Column(nullable = false)
    private Long portfolioId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @OneToMany(mappedBy = "execution", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderRecord> orderRecords = new ArrayList<>();

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public static RebalancingExecution create(Long jobId, Long portfolioId) {
        RebalancingExecution exec = new RebalancingExecution();
        exec.jobId = jobId;
        exec.portfolioId = portfolioId;
        exec.status = ExecutionStatus.PENDING;
        return exec;
    }

    public void startProcessing() {
        this.status = ExecutionStatus.PROCESSING;
    }

    public void complete() {
        this.status = ExecutionStatus.COMPLETED;
    }

    public void fail() {
        this.status = ExecutionStatus.FAILED;
    }

    public boolean isCompleted() {
        return this.status == ExecutionStatus.COMPLETED;
    }

    public boolean isProcessing() {
        return this.status == ExecutionStatus.PROCESSING;
    }

    public boolean isFailed() {
        return this.status == ExecutionStatus.FAILED;
    }

    public boolean isTerminal() {
        return this.status == ExecutionStatus.COMPLETED || this.status == ExecutionStatus.FAILED;
    }

    public List<OrderRecord> getPendingOrders() {
        return orderRecords.stream()
                .filter(o -> o.getStatus() == OrderStatus.PENDING)
                .toList();
    }

    public List<OrderRecord> getCompletedOrders() {
        return orderRecords.stream()
                .filter(o -> o.getStatus() == OrderStatus.COMPLETED)
                .toList();
    }
}
