package com.rebra.repository;

import com.rebra.entity.Stock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    /**
     * 종목 코드로 주식 조회
     */
    Optional<Stock> findByStockCode(String stockCode);

    /**
     * 활성 상태인 종목 코드로 주식 조회
     */
    Optional<Stock> findByStockCodeAndIsActiveTrue(String stockCode);

    /**
     * 종목 코드 존재 여부 확인
     */
    boolean existsByStockCode(String stockCode);

    /**
     * 종목명으로 주식 조회
     */
    Optional<Stock> findByStockName(String stockName);

    /**
     * 활성 상태인 종목명으로 주식 조회
     */
    Optional<Stock> findByStockNameAndIsActiveTrue(String stockName);

    /**
     * 종목명에 특정 문자열이 포함된 주식 목록 조회 (활성 상태만, 페이지네이션)
     */
    Page<Stock> findByStockNameContainingIgnoreCaseAndIsActiveTrue(String stockName, Pageable pageable);

    /**
     * 종목명에 특정 문자열이 포함된 주식 목록 조회 (모든 상태, 페이지네이션)
     */
    Page<Stock> findByStockNameContainingIgnoreCase(String stockName, Pageable pageable);
}