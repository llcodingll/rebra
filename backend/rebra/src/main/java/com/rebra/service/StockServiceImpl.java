package com.rebra.service;

import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;

    @Override
    public StockSearchResponse findByStockCode(String stockCode) {
        return stockRepository.findByStockCodeAndIsActiveTrue(stockCode)
            .map(StockSearchResponse::from)
            .orElseThrow(StockException::stockCodeNotFound);
    }

    @Override
    public StockSearchResponse findByStockName(String stockName) {
        return stockRepository.findByStockNameAndIsActiveTrue(stockName)
            .map(StockSearchResponse::from)
            .orElseThrow(StockException::stockNameNotFound);
    }

    @Override
    public List<StockSearchResponse> searchByStockName(String stockName) {
        return stockRepository.findByStockNameContainingIgnoreCase(stockName)
            .stream()
            .map(StockSearchResponse::from)
            .toList();
    }

    @Override
    public List<StockSearchResponse> searchActiveStocksByName(String stockName) {
        return stockRepository.findByStockNameContainingIgnoreCaseAndIsActiveTrue(stockName)
            .stream()
            .map(StockSearchResponse::from)
            .toList();
    }
}