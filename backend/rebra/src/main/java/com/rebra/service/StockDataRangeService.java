package com.rebra.service;

import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockDataRangeService {
    
    private final StockRepository stockRepository;
    
    /**
     * Stock 데이터 범위 업데이트 - 별도 트랜잭션으로 즉시 커밋
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateStockDataRange(String stockCode, LocalDate start, LocalDate end) {
        log.info("updateStockDataRange 시작 (새 트랜잭션) - 종목: {}, 요청범위: {} ~ {}", 
            stockCode, start, end);
        
        Stock stock = stockRepository.findByStockCode(stockCode)
            .orElseThrow(() -> StockException.stockCodeNotFound());
        
        log.info("Stock 조회 완료 - 현재 범위: {} ~ {}", 
            stock.getDataStartDate(), stock.getDataEndDate());
        
        stock.updateDataRange(start, end);
        stockRepository.saveAndFlush(stock);
        
        log.info("Stock 저장 및 flush 완료 (새 트랜잭션) - 새 범위: {} ~ {}", 
            stock.getDataStartDate(), stock.getDataEndDate());
    }
}