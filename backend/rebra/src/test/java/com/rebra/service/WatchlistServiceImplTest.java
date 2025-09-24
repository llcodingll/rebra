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
import com.rebra.entity.Stock;
import com.rebra.entity.User;
import com.rebra.entity.Watchlist;
import com.rebra.exception.CustomRuntimeException;
import com.rebra.exception.ExceptionCode;
import com.rebra.repository.StockRepository;
import com.rebra.repository.UserRepository;
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

    private static final Long USER_ID = 1L;
    private static final Long STOCK_ID = 1L;
    private static final String STOCK_CODE = "005930";
    private static final String STOCK_NAME = "삼성전자";
    private static final String INVALID_STOCK_CODE = "INVALID";
    private static final Long INVALID_USER_ID = 999L;

    @Mock
    private WatchlistRepository watchlistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private WatchlistServiceImpl watchlistService;

    private User testUser;
    private Stock testStock;
    private Watchlist testWatchlist;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testStock = createTestStock();
        testWatchlist = createTestWatchlist(testUser, testStock);
    }

    @Nested
    @DisplayName("관심종목 토글")
    class ToggleWatchlistTest {

        @Test
        @DisplayName("성공: 관심종목 추가")
        void toggleWatchlist_Add_Success() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.findByStockCode(STOCK_CODE)).willReturn(Optional.of(testStock));
            given(watchlistRepository.findByUserIdAndStockCode(USER_ID, STOCK_CODE)).willReturn(Optional.empty());
            given(userRepository.findById(USER_ID)).willReturn(Optional.of(testUser));
            given(watchlistRepository.save(any(Watchlist.class))).willReturn(testWatchlist);

            // When
            WatchlistToggleResponse response = watchlistService.toggleWatchlist(USER_ID, STOCK_CODE);

            // Then
            assertThat(response.isAdded()).isTrue();
            assertThat(response.getMessage()).isEqualTo("관심종목에 추가되었습니다");
            assertThat(response.getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(response.getStockName()).isEqualTo(STOCK_NAME);

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).findByStockCode(STOCK_CODE);
            verify(watchlistRepository).findByUserIdAndStockCode(USER_ID, STOCK_CODE);
            verify(userRepository).findById(USER_ID);
            verify(watchlistRepository).save(any(Watchlist.class));
            verify(watchlistRepository, never()).deleteByUserIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("성공: 관심종목 제거")
        void toggleWatchlist_Remove_Success() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.findByStockCode(STOCK_CODE)).willReturn(Optional.of(testStock));
            given(watchlistRepository.findByUserIdAndStockCode(USER_ID, STOCK_CODE)).willReturn(Optional.of(testWatchlist));

            // When
            WatchlistToggleResponse response = watchlistService.toggleWatchlist(USER_ID, STOCK_CODE);

            // Then
            assertThat(response.isAdded()).isFalse();
            assertThat(response.getMessage()).isEqualTo("관심종목에서 제거되었습니다");
            assertThat(response.getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(response.getStockName()).isEqualTo(STOCK_NAME);

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).findByStockCode(STOCK_CODE);
            verify(watchlistRepository).findByUserIdAndStockCode(USER_ID, STOCK_CODE);
            verify(watchlistRepository).deleteByUserIdAndStockCode(USER_ID, STOCK_CODE);
            verify(userRepository, never()).findById(any());
            verify(watchlistRepository, never()).save(any());
        }

        @Test
        @DisplayName("실패: 사용자 존재하지 않음")
        void toggleWatchlist_UserNotFound() {
            // Given
            given(userRepository.existsById(INVALID_USER_ID)).willReturn(false);

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.toggleWatchlist(INVALID_USER_ID, STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);

            verify(userRepository).existsById(INVALID_USER_ID);
            verify(stockRepository, never()).findByStockCode(any());
            verify(watchlistRepository, never()).findByUserIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("실패: 종목 존재하지 않음")
        void toggleWatchlist_StockNotFound() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.findByStockCode(INVALID_STOCK_CODE)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.toggleWatchlist(USER_ID, INVALID_STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.STOCK_CODE_NOT_FOUND);

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).findByStockCode(INVALID_STOCK_CODE);
            verify(watchlistRepository, never()).findByUserIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("실패: 관심종목 추가 시 사용자 조회 실패")
        void toggleWatchlist_UserNotFoundWhenAdding() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.findByStockCode(STOCK_CODE)).willReturn(Optional.of(testStock));
            given(watchlistRepository.findByUserIdAndStockCode(USER_ID, STOCK_CODE)).willReturn(Optional.empty());
            given(userRepository.findById(USER_ID)).willReturn(Optional.empty());

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.toggleWatchlist(USER_ID, STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).findByStockCode(STOCK_CODE);
            verify(watchlistRepository).findByUserIdAndStockCode(USER_ID, STOCK_CODE);
            verify(userRepository).findById(USER_ID);
            verify(watchlistRepository, never()).save(any());
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
            Watchlist watchlist2 = createTestWatchlist(testUser, stock2);
            List<Watchlist> watchlists = List.of(testWatchlist, watchlist2);

            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(watchlistRepository.findByUserIdWithStock(USER_ID)).willReturn(watchlists);

            // When
            List<WatchlistDto> result = watchlistService.getAllWatchlist(USER_ID);

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStockCode()).isEqualTo(STOCK_CODE);
            assertThat(result.get(0).getStockName()).isEqualTo(STOCK_NAME);
            assertThat(result.get(1).getStockCode()).isEqualTo("000660");
            assertThat(result.get(1).getStockName()).isEqualTo("SK하이닉스");

            verify(userRepository).existsById(USER_ID);
            verify(watchlistRepository).findByUserIdWithStock(USER_ID);
        }

        @Test
        @DisplayName("성공: 관심종목 없음")
        void getAllWatchlist_EmptyList() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(watchlistRepository.findByUserIdWithStock(USER_ID)).willReturn(List.of());

            // When
            List<WatchlistDto> result = watchlistService.getAllWatchlist(USER_ID);

            // Then
            assertThat(result).isEmpty();

            verify(userRepository).existsById(USER_ID);
            verify(watchlistRepository).findByUserIdWithStock(USER_ID);
        }

        @Test
        @DisplayName("실패: 사용자 존재하지 않음")
        void getAllWatchlist_UserNotFound() {
            // Given
            given(userRepository.existsById(INVALID_USER_ID)).willReturn(false);

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.getAllWatchlist(INVALID_USER_ID));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);

            verify(userRepository).existsById(INVALID_USER_ID);
            verify(watchlistRepository, never()).findByUserIdWithStock(any());
        }
    }

    @Nested
    @DisplayName("관심종목 상태 확인")
    class IsInWatchlistTest {

        @Test
        @DisplayName("성공: 관심종목에 등록된 상태")
        void isInWatchlist_True() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.existsByStockCode(STOCK_CODE)).willReturn(true);
            given(watchlistRepository.existsByUserIdAndStockCode(USER_ID, STOCK_CODE)).willReturn(true);

            // When
            boolean result = watchlistService.isInWatchlist(USER_ID, STOCK_CODE);

            // Then
            assertThat(result).isTrue();

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).existsByStockCode(STOCK_CODE);
            verify(watchlistRepository).existsByUserIdAndStockCode(USER_ID, STOCK_CODE);
        }

        @Test
        @DisplayName("성공: 관심종목에 등록되지 않은 상태")
        void isInWatchlist_False() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.existsByStockCode(STOCK_CODE)).willReturn(true);
            given(watchlistRepository.existsByUserIdAndStockCode(USER_ID, STOCK_CODE)).willReturn(false);

            // When
            boolean result = watchlistService.isInWatchlist(USER_ID, STOCK_CODE);

            // Then
            assertThat(result).isFalse();

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).existsByStockCode(STOCK_CODE);
            verify(watchlistRepository).existsByUserIdAndStockCode(USER_ID, STOCK_CODE);
        }

        @Test
        @DisplayName("실패: 사용자 존재하지 않음")
        void isInWatchlist_UserNotFound() {
            // Given
            given(userRepository.existsById(INVALID_USER_ID)).willReturn(false);

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.isInWatchlist(INVALID_USER_ID, STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.USER_NOT_FOUND);

            verify(userRepository).existsById(INVALID_USER_ID);
            verify(stockRepository, never()).existsByStockCode(any());
            verify(watchlistRepository, never()).existsByUserIdAndStockCode(any(), any());
        }

        @Test
        @DisplayName("실패: 종목 존재하지 않음")
        void isInWatchlist_StockNotFound() {
            // Given
            given(userRepository.existsById(USER_ID)).willReturn(true);
            given(stockRepository.existsByStockCode(INVALID_STOCK_CODE)).willReturn(false);

            // When & Then
            CustomRuntimeException exception = assertThrows(CustomRuntimeException.class,
                    () -> watchlistService.isInWatchlist(USER_ID, INVALID_STOCK_CODE));

            assertThat(exception.getExceptionCode()).isEqualTo(ExceptionCode.STOCK_CODE_NOT_FOUND);

            verify(userRepository).existsById(USER_ID);
            verify(stockRepository).existsByStockCode(INVALID_STOCK_CODE);
            verify(watchlistRepository, never()).existsByUserIdAndStockCode(any(), any());
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

    private Watchlist createTestWatchlist(User user, Stock stock) {
        Watchlist watchlist = Watchlist.builder()
                .user(user)
                .stock(stock)
                .build();
        ReflectionTestUtils.setField(watchlist, "id", 1L);
        return watchlist;
    }
}