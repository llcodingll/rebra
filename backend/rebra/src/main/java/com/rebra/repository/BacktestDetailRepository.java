package com.rebra.repository;

import com.rebra.entity.BacktestDetail;
import com.rebra.entity.BacktestRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BacktestDetailRepository extends JpaRepository<BacktestDetail, Long> {

    /**
     * 백테스트 기록으로 상세 결과 조회 (날짜 순 정렬)
     */
    List<BacktestDetail> findByBacktestRecordOrderByPeriodDateAsc(BacktestRecord backtestRecord);

    /**
     * 백테스트 기록 ID로 상세 결과 조회
     */
    List<BacktestDetail> findByBacktestRecordIdOrderByPeriodDateAsc(Long backtestRecordId);

    /**
     * 특정 기간의 상세 결과 조회
     */
    List<BacktestDetail> findByBacktestRecordAndPeriodDateBetweenOrderByPeriodDateAsc(
            BacktestRecord backtestRecord, LocalDate startDate, LocalDate endDate);

    /**
     * 리밸런싱이 발생한 기간들 조회
     */
    List<BacktestDetail> findByBacktestRecordAndIsRebalancedTrueOrderByPeriodDateAsc(BacktestRecord backtestRecord);

}