package com.rebra.repository;

import com.rebra.entity.BacktestRecord;
import com.rebra.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BacktestRecordRepository extends JpaRepository<BacktestRecord, Long> {

    /**
     * 사용자별 백테스트 기록 조회 (페이징)
     */
    Page<BacktestRecord> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * 사용자별 백테스트 기록 조회 (전체)
     */
    List<BacktestRecord> findByUserOrderByCreatedAtDesc(User user);

    /**
     * 특정 상태의 백테스트 기록 조회
     */
    List<BacktestRecord> findByStatusOrderByCreatedAtAsc(BacktestRecord.BacktestStatus status);

    /**
     * 사용자의 특정 상태 백테스트 기록 조회
     */
    List<BacktestRecord> findByUserAndStatusOrderByCreatedAtDesc(User user, BacktestRecord.BacktestStatus status);

    /**
     * 사용자의 백테스트 기록과 결과를 함께 조회
     */
    @Query("SELECT br FROM BacktestRecord br LEFT JOIN FETCH br.user WHERE br.user = :user AND br.id = :id")
    Optional<BacktestRecord> findByUserAndIdWithUser(@Param("user") User user, @Param("id") Long id);

    /**
     * 진행 중인 백테스트 개수 조회
     */
    @Query("SELECT COUNT(br) FROM BacktestRecord br WHERE br.status IN ('PENDING', 'PROCESSING')")
    Long countInProgressBacktests();

    /**
     * 사용자의 진행 중인 백테스트 개수 조회
     */
    @Query("SELECT COUNT(br) FROM BacktestRecord br WHERE br.user = :user AND br.status IN ('PENDING', 'PROCESSING')")
    Long countInProgressBacktestsByUser(@Param("user") User user);

    /**
     * 오래된 대기 중인 백테스트 조회 (타임아웃 처리용)
     */
    @Query("SELECT br FROM BacktestRecord br WHERE br.status = 'PENDING' AND br.createdAt < :cutoffTime")
    List<BacktestRecord> findOldPendingBacktests(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * 특정 기간의 백테스트 기록 조회
     */
    List<BacktestRecord> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 사용자의 최근 백테스트 기록 조회
     */
    @Query("SELECT br FROM BacktestRecord br WHERE br.user = :user ORDER BY br.createdAt DESC")
    List<BacktestRecord> findRecentBacktestsByUser(@Param("user") User user, Pageable pageable);

}