package com.rebra.service;

import com.rebra.dto.response.PageResponse;
import com.rebra.dto.response.RealtimeStockData;
import com.rebra.dto.response.StockDetailResponse;
import com.rebra.dto.response.StockSearchResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Stock;
import com.rebra.exception.stock.StockException;
import com.rebra.repository.AccountRepository;
import com.rebra.repository.StockRepository;
import com.rebra.service.KisRealtimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockServiceImpl implements StockService {

    private final StockRepository stockRepository;
    private final AccountRepository accountRepository;
    private final KisRealtimeService kisRealtimeService;
    // Redis 캐싱 제거 - 프론트엔드에서 실시간 데이터 관리
    // 실시간 데이터는 WebSocket을 통해 직접 클라이언트로 전달

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
    public PageResponse<StockSearchResponse> searchStocks(String stockName, Pageable pageable) {
        Page<Stock> stockPage = stockRepository.findByStockNameContainingIgnoreCaseAndIsActiveTrue(stockName, pageable);
        Page<StockSearchResponse> dtoPage = stockPage.map(StockSearchResponse::from);
        return PageResponse.from(dtoPage);
    }

    @Override
    public StockDetailResponse getStockDetailWithRealtime(String stockCode, Long userId) {
        // 기존 즉시 호출 방식은 더 이상 사용하지 않음
        // 프론트엔드에서 WebSocket 채널 정보를 받고 직접 구독
        return getStockDetailWithWebSocketInfo(stockCode, userId);
    }
    
    @Override
    public StockDetailResponse getStockDetailWithWebSocketInfo(String stockCode, Long userId) {
        try {
            // 주식 기본 정보 조회
            Stock stock = stockRepository.findByStockCodeAndIsActiveTrue(stockCode)
                    .orElseThrow(StockException::stockCodeNotFound);

            log.info("종목 상세 정보 조회 완료 (WebSocket 방식) - UserId: {}, StockCode: {}",
                    userId, stockCode);

            // 사용자 계좌 정보 조회
            Account account = accountRepository.findTopByUserIdAndIsDeletedFalseOrderByCreatedAtAsc(userId)
                    .orElseThrow(() -> new RuntimeException("활성화된 계좌를 찾을 수 없습니다."));

            // 즉시 한국투자증권 실시간 데이터 구독 시작
            String sessionId = "api-request-" + userId + "-" + stockCode;

            log.info("KIS 실시간 데이터 구독 시작 - UserId: {}, StockCode: {}, SessionId: {}",
                    userId, stockCode, sessionId);

            // 실시간 체결가 구독 시작
            kisRealtimeService.startPriceSubscription(account, stockCode, sessionId);

            // 실시간 호가 구독 시작
            kisRealtimeService.startOrderbookSubscription(account, stockCode, sessionId);

            // WebSocket 채널 정보와 함께 반환
            return StockDetailResponse.ofWithWebSocketInfo(stock, userId, stockCode);

        } catch (Exception e) {
            log.error("종목 상세 정보 조회 실패 (WebSocket 방식) - UserId: {}, StockCode: {}",
                    userId, stockCode, e);
            throw new RuntimeException("종목 상세 정보 조회에 실패했습니다.", e);
        }
    }
}