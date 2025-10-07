package com.rebra.repository;

import com.rebra.entity.OutboxEvent;
import com.rebra.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop10ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
