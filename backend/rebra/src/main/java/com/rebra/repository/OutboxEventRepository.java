package com.rebra.repository;

import com.rebra.entity.OutboxEvent;
import com.rebra.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = com.rebra.entity.OutboxStatus.INIT, o.publishedAt = null WHERE o.status = com.rebra.entity.OutboxStatus.PUBLISHED")
    void resetAllPublishedToInit();

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = com.rebra.entity.OutboxStatus.INIT, o.publishedAt = null WHERE o.status = com.rebra.entity.OutboxStatus.PUBLISHED AND o.publishedAt < :threshold")
    int resetStalePublishedToInit(@Param("threshold") LocalDateTime threshold);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.status = com.rebra.entity.OutboxStatus.CONSUMED WHERE o.rebalancingOrderId = :rebalancingOrderId AND o.status IN (com.rebra.entity.OutboxStatus.INIT, com.rebra.entity.OutboxStatus.PUBLISHED)")
    void markConsumedByRebalancingOrderId(@Param("rebalancingOrderId") Long rebalancingOrderId);
}
