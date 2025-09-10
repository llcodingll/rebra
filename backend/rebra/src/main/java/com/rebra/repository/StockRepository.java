package com.rebra.repository;

import com.rebra.entity.Stock;
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
}