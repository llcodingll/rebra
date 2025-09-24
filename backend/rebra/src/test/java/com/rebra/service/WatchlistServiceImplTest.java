package com.rebra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;

import com.rebra.dto.WatchlistDto;
import com.rebra.dto.response.WatchlistToggleResponse;
import com.rebra.entity.Account;
import com.rebra.entity.Portfolio;
import com.rebra.entity.Stock;
import com.rebra.entity.User;
import com.rebra.entity.Watchlist;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.PortfolioRepository;
import com.rebra.repository.StockRepository;
import com.rebra.repository.WatchlistRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class WatchlistServiceImplTest {

    private static final Long ACCOUNT_ID = 1L;
    private static final Long PORTFOLIO_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long STOCK_ID = 1L;
    private static final String STOCK_CODE = "005930";
    private static final String STOCK_NAME = "삼성전자";
    private static final String INVALID_STOCK_CODE = "INVALID";
    private static final Long INVALID_ACCOUNT_ID = 999L;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private WatchlistServiceImpl watchlistService;

    private User testUser;
    private Account testAccount;
    private Portfolio testPortfolio;
    private Stock testStock;
    private Watchlist testWatchlist;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testAccount = createTestAccount();
        testPortfolio = createTestPortfolio();
        testStock = createTestStock();
        testWatchlist = createTestWatchlist(testPortfolio, testStock);
    }

    @Nested
    @DisplayName("관심종목 토글")
    class ToggleWatchlistTest {

        @Test
        @DisplayName("성공: 관심종목 추가")
        void toggleWatchlist_Add_Success() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.findByStockCode(STOCK_CODE)).willReturn(Optional.of(testStock));
            given(watchlistRepository.findByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE)).willReturn(Optional.empty());
            given(watchlistRepository.save(any(Watchlist.class))).willReturn(testWatchlist);

            // When
            WatchlistToggleResponse response = watchlistService.toggleWatchlist(ACCOUNT_ID, STOCK_CODE);

            // Then
            assertThat(response.isAdded()).isTrue();
            assertThat(response.getMessage()).isEqualTo("관심종목에 추가되었습니다");
            assertThat(response.getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(response.getStockName()).isEqualTo(STOCK_NAME);

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).findByStockCode(STOCK_CODE);
            verify(watchlistRepository).findByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE);
            verify(watchlistRepository).save(any(Watchlist.class));
            verify(watchlistRepository, never()).deleteByPortfolioIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("성공: 관심종목 제거")
        void toggleWatchlist_Remove_Success() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.findByStockCode(STOCK_CODE)).willReturn(Optional.of(testStock));
            given(watchlistRepository.findByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE)).willReturn(Optional.of(testWatchlist));

            // When
            WatchlistToggleResponse response = watchlistService.toggleWatchlist(ACCOUNT_ID, STOCK_CODE);

            // Then
            assertThat(response.isAdded()).isFalse();
            assertThat(response.getMessage()).isEqualTo("관심종목에서 제거되었습니다");
            assertThat(response.getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(response.getStockName()).isEqualTo(STOCK_NAME);

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).findByStockCode(STOCK_CODE);
            verify(watchlistRepository).findByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE);
            verify(watchlistRepository).deleteByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE);
            verify(watchlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 포트폴리오 존재하지 않음")
        void toggleWatchlist_PortfolioNotFound() {
            // Given
            given(portfolioRepository.findByAccountId(INVALID_ACCOUNT_ID)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.toggleWatchlist(INVALID_ACCOUNT_ID, STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.PORTFOLIO_NOT_FOUND);

            verify(portfolioRepository).findByAccountId(INVALID_ACCOUNT_ID);
            verify(stockRepository, never()).findByStockCode(any());
            verify(watchlistRepository, never()).findByPortfolioIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("실패: 종목 존재하지 않음")
        void toggleWatchlist_StockNotFound() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.findByStockCode(INVALID_STOCK_CODE)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.toggleWatchlist(ACCOUNT_ID, INVALID_STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.STOCK_CODE_NOT_FOUND);

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).findByStockCode(INVALID_STOCK_CODE);
            verify(watchlistRepository, never()).findByPortfolioIdAndStockCode(any(), any());
        }

    }

    @Nested
    @DisplayName("관심종목 전체 조회")
    class GetAllWatchlistTest {

        @Test
        @DisplayName("성공: 관심종목 리스트 조회")
        void getAllWatchlist_Success() {
            // Given
            Stock stock2 = createAnotherTestStock();
            Watchlist watchlist2 = createTestWatchlist(testPortfolio, stock2);
            List<Watchlist> watchlists = List.of(testWatchlist, watchlist2);

            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(watchlistRepository.findByPortfolioIdWithStock(PORTFOLIO_ID)).willReturn(watchlists);

            // When
            List<WatchlistDto> result = watchlistService.getAllWatchlist(ACCOUNT_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(result.get(0).getStockName()).isEqualTo(STOCK_NAME);
            assertThat(result.get(1).getStockCode()).isEqualTo("000660");
            assertThat(result.get(1).getStockName()).isEqualTo("SK하이닉스");

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(watchlistRepository).findByPortfolioIdWithStock(PORTFOLIO_ID);
        }

        @Test
        @DisplayName("성공: 관심종목 없음")
        void getAllWatchlist_EmptyList() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(watchlistRepository.findByPortfolioIdWithStock(PORTFOLIO_ID)).willReturn(List.of());

            // When
            List<WatchlistDto> result = watchlistService.getAllWatchlist(ACCOUNT_ID);

            // Then
            assertThat(result).isEmpty();

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(watchlistRepository).findByPortfolioIdWithStock(PORTFOLIO_ID);
        }

        @Test
        @DisplayName("실패: 포트폴리오 존재하지 않음")
        void getAllWatchlist_PortfolioNotFound() {
            // Given
            given(portfolioRepository.findByAccountId(INVALID_ACCOUNT_ID)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.getAllWatchlist(INVALID_ACCOUNT_ID));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.PORTFOLIO_NOT_FOUND);

            verify(portfolioRepository).findByAccountId(INVALID_ACCOUNT_ID);
            verify(watchlistRepository, never()).findByPortfolioIdWithStock(any());
        }
    }

    @Nested
    @DisplayName("관심종목 상태 확인")
    class IsInWatchlistTest {

        @Test
        @DisplayName("성공: 관심종목에 등록된 상태")
        void isInWatchlist_True() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.existsByStockCode(STOCK_CODE)).willReturn(true);
            given(watchlistRepository.existsByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE)).willReturn(true);

            // When
            boolean result = watchlistService.isInWatchlist(ACCOUNT_ID, STOCK_CODE);

            // Then
            assertThat(result).isTrue();

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).existsByStockCode(STOCK_CODE);
            verify(watchlistRepository).existsByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE);
        }

        @Test
        @DisplayName("성공: 관심종목에 등록되지 않은 상태")
        void isInWatchlist_False() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.existsByStockCode(STOCK_CODE)).willReturn(true);
            given(watchlistRepository.existsByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE)).willReturn(false);

            // When
            boolean result = watchlistService.isInWatchlist(ACCOUNT_ID, STOCK_CODE);

            // Then
            assertThat(result).isFalse();

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).existsByStockCode(STOCK_CODE);
            verify(watchlistRepository).existsByPortfolioIdAndStockCode(PORTFOLIO_ID, STOCK_CODE);
        }

        @Test
        @DisplayName("실패: 포트폴리오 존재하지 않음")
        void isInWatchlist_PortfolioNotFound() {
            // Given
            given(portfolioRepository.findByAccountId(INVALID_ACCOUNT_ID)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.isInWatchlist(INVALID_ACCOUNT_ID, STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.PORTFOLIO_NOT_FOUND);

            verify(portfolioRepository).findByAccountId(INVALID_ACCOUNT_ID);
            verify(stockRepository, never()).existsByStockCode(any());
            verify(watchlistRepository, never()).existsByPortfolioIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("실패: 종목 존재하지 않음")
        void isInWatchlist_StockNotFound() {
            // Given
            given(portfolioRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(testPortfolio));
            given(stockRepository.existsByStockCode(INVALID_STOCK_CODE)).willReturn(false);

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.isInWatchlist(ACCOUNT_ID, INVALID_STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.STOCK_CODE_NOT_FOUND);

            verify(portfolioRepository).findByAccountId(ACCOUNT_ID);
            verify(stockRepository).existsByStockCode(INVALID_STOCK_CODE);
            verify(watchlistRepository, never()).existsByPortfolioIdAndStockCode(any(), any());
        }
    }

    // Helper Methods
    private User createTestUser() {
        User user = User.builder()
                .sub("test-user")
                .nickname("테스트사용자")
                .build();
        ReflectionTestUtils.setField(user, "id", USER_ID);
        return user;
    }

    private Stock createTestStock() {
        Stock stock = Stock.builder()
                .stockCode(STOCK_CODE)
                .stockName(STOCK_NAME)
                .stockType("주식")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(stock, "id", STOCK_ID);
        return stock;
    }

    private Stock createAnotherTestStock() {
        Stock stock = Stock.builder()
                .stockCode("000660")
                .stockName("SK하이닉스")
                .stockType("주식")
                .isActive(true)
                .build();
        ReflectionTestUtils.setField(stock, "id", 2L);
        return stock;
    }

    private Account createTestAccount() {
        Account account = Account.builder()
                .user(testUser)
                .accountNumber("encrypted-account-number")
                .accountNumberHash("hash-value")
                .appKey("encrypted-app-key")
                .appSecret("encrypted-app-secret")
                .brokerName("테스트증권")
                .accountType(null)
                .isConnected(true)
                .build();
        ReflectionTestUtils.setField(account, "id", ACCOUNT_ID);
        return account;
    }

    private Portfolio createTestPortfolio() {
        Portfolio portfolio = Portfolio.builder()
                .user(testUser)
                .account(testAccount)
                .name("테스트 포트폴리오")
                .description("테스트용 포트폴리오")
                .build();
        ReflectionTestUtils.setField(portfolio, "id", PORTFOLIO_ID);
        return portfolio;
    }

    private Watchlist createTestWatchlist(Portfolio portfolio, Stock stock) {
        Watchlist watchlist = Watchlist.builder()
                .portfolio(portfolio)
                .stock(stock)
                .build();
        ReflectionTestUtils.setField(watchlist, "id", 1L);
        return watchlist;
    }
}