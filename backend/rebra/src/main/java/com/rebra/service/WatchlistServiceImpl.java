package com.rebra.service;

import com.rebra.dto.WatchlistDto;
import com.rebra.dto.response.WatchlistToggleResponse;
import com.rebra.entity.Stock;
import com.rebra.entity.Portfolio;
import com.rebra.entity.Watchlist;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.StockRepository;
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
    private final PortfolioRepository portfolioRepository;
    private final StockRepository stockRepository;

    @Override
    public WatchlistToggleResponse toggleWatchlist(Long accountId, String stockCode) {
        log.info("관심종목 토글 요청 - AccountId: {}, StockCode: {}", accountId, stockCode);

        Portfolio portfolio = validateAndGetPortfolio(accountId);
        Stock stock = validateAndGetStock(stockCode);

        Optional<Watchlist> existingWatchlist = watchlistRepository.findByPortfolioIdAndStockCode(portfolio.getId(), stockCode);

        if (existingWatchlist.isPresent()) {
            return removeFromWatchlist(portfolio, stockCode, stock);
        }

        return addToWatchlist(portfolio, stockCode, stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WatchlistDto> getAllWatchlist(Long accountId) {
        log.info("관심종목 전체 조회 요청 - AccountId: {}", accountId);

        Portfolio portfolio = validateAndGetPortfolio(accountId);

        return watchlistRepository.findByPortfolioIdWithStock(portfolio.getId())
            .stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInWatchlist(Long accountId, String stockCode) {
        log.info("관심종목 상태 확인 요청 - AccountId: {}, StockCode: {}", accountId, stockCode);

        Portfolio portfolio = validateAndGetPortfolio(accountId);
        validateStockExists(stockCode);

        return watchlistRepository.existsByPortfolioIdAndStockCode(portfolio.getId(), stockCode);
    }

    private Portfolio validateAndGetPortfolio(Long accountId) {
        return portfolioRepository.findByAccountId(accountId)
            .orElseThrow(() -> new CustomRuntimeException(ExceptionCode.PORTFOLIO_NOT_FOUND));
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

    private WatchlistToggleResponse removeFromWatchlist(Portfolio portfolio, String stockCode, Stock stock) {
        watchlistRepository.deleteByPortfolioIdAndStockCode(portfolio.getId(), stockCode);
        log.info("관심종목 제거됨 - PortfolioId: {}, StockCode: {}, StockName: {}", portfolio.getId(), stockCode, stock.getStockName());

        return new WatchlistToggleResponse(
            false,
            "관심종목에서 제거되었습니다",
            stockCode,
            stock.getStockName()
        );
    }

    private WatchlistToggleResponse addToWatchlist(Portfolio portfolio, String stockCode, Stock stock) {
        Watchlist watchlist = Watchlist.builder()
            .portfolio(portfolio)
            .stock(stock)
            .build();

        watchlistRepository.save(watchlist);
        log.info("관심종목 추가됨 - PortfolioId: {}, StockCode: {}, StockName: {}", portfolio.getId(), stockCode, stock.getStockName());

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
