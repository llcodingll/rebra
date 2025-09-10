package com.rebra.repository;

import com.rebra.entity.BacktestRecord;
import com.rebra.entity.BacktestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BacktestResultRepository extends JpaRepository<BacktestResult, Long> {

    /**
     * 백테스트 기록으로 결과 조회
     */
    Optional<BacktestResult> findByBacktestRecord(BacktestRecord backtestRecord);

    /**
     * 백테스트 기록 ID로 결과 조회
     */
    Optional<BacktestResult> findByBacktestRecordId(Long backtestRecordId);

}