package com.rebra.service;

import com.rebra.dto.WatchlistDto;
import com.rebra.dto.response.WatchlistToggleResponse;
import com.rebra.entity.Stock;
import com.rebra.entity.User;
import com.rebra.entity.Watchlist;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.StockRepository;
import com.rebra.repository.UserRepository;
import com.rebra.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WatchlistServiceImpl implements WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    @Override
    public WatchlistToggleResponse toggleWatchlist(Long userId, String stockCode) {
        log.info("관심종목 토글 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        validateUser(userId);
        Stock stock = validateAndGetStock(stockCode);

        Optional<Watchlist> existingWatchlist = watchlistRepository.findByUserIdAndStockCode(userId, stockCode);

        if (existingWatchlist.isPresent()) {
            return removeFromWatchlist(userId, stockCode, stock);
        }

        return addToWatchlist(userId, stockCode, stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistDto> getAllWatchlist(Long userId) {
        log.info("관심종목 전체 조회 요청 - UserId: {}", userId);

        validateUser(userId);

        return watchlistRepository.findByUserIdWithStock(userId)
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWatchlist(Long userId, String stockCode) {
        log.info("관심종목 상태 확인 요청 - UserId: {}, StockCode: {}", userId, stockCode);

        validateUser(userId);
        validateStockExists(stockCode);

        return watchlistRepository.existsByUserIdAndStockCode(userId, stockCode);
    }

    // === Private Helper Methods ===

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new CustomRuntimeException(ExceptionCode.USER_NOT_FOUND);
        }
    }

    private Stock validateAndGetStock(String stockCode) {
        return stockRepository.findByStockCode(stockCode)
            .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.STOCK_CODE_NOT_FOUND));
    }

    private void validateStockExists(String stockCode) {
        if (!stockRepository.existsByStockCode(stockCode)) {
            throw new CustomRuntimeException(ExceptionCode.STOCK_CODE_NOT_FOUND);
        }
    }

    private WatchlistToggleResponse removeFromWatchlist(Long userId, String stockCode, Stock stock) {
        watchlistRepository.deleteByUserIdAndStockCode(userId, stockCode);
        log.info("관심종목 제거됨 - UserId: {}, StockCode: {}, StockName: {}", userId, stockCode, stock.getStockName());

        return new WatchlistToggleResponse(
            false,
            "관심종목에서 제거되었습니다",
            stockCode,
            stock.getStockName()
        );
    }

    private WatchlistToggleResponse addToWatchlist(Long userId, String stockCode, Stock stock) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.USER_NOT_FOUND));

        Watchlist watchlist = Watchlist.builder()
            .user(user)
            .stock(stock)
            .build();

        watchlistRepository.save(watchlist);
        log.info("관심종목 추가됨 - UserId: {}, StockCode: {}, StockName: {}", userId, stockCode, stock.getStockName());

        return new WatchlistToggleResponse(
            true,
            "관심종목에 추가되었습니다",
            stockCode,
            stock.getStockName()
        );
    }

    private WatchlistDto convertToDto(Watchlist watchlist) {
        return new WatchlistDto(
            watchlist.getStock().getStockCode(),
            watchlist.getStock().getStockName()
        );
    }
}
