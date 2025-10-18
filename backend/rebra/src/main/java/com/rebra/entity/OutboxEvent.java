package com.rebra.entity;

import com.rebra.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "outbox_event")
public class OutboxEvent extends BaseEntity {

    @Id
    @SequenceGenerator(name = "outbox_seq", sequenceName = "outbox_event_seq", allocationSize = 50)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "outbox_seq")
    private Long id;

    @Column(nullable = false, unique = true)
    private Long rebalancingOrderId;

    @Column(nullable = false)
    private Long portfolioId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;         // JSON: RebalancingOrderMessage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    private LocalDateTime publishedAt;

    public static OutboxEvent create(Long rebalancingOrderId, Long portfolioId, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.rebalancingOrderId = rebalancingOrderId;
        event.portfolioId = portfolioId;
        event.payload = payload;
        event.status = OutboxStatus.INIT;
        return event;
    }

    public void markPublished() {
        this.status = OutboxStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }

    public void markConsumed() {
        this.status = OutboxStatus.CONSUMED;
    }
}
