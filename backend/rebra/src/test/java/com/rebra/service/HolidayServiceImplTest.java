package com.rebra.service;

import com.rebra.client.HolidayApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HolidayServiceImplTest {

    @Mock
    private HolidayApiClient holidayApiClient;

    @InjectMocks
    private HolidayServiceImpl holidayService;

    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("holidayCache");
        ReflectionTestUtils.setField(holidayService, "cacheManager", cacheManager);
    }

    @Test
    @DisplayName("주말(토요일)은 거래일이 아니다")
    void isTradingDay_Saturday_ReturnsFalse() {
        // given
        LocalDate saturday = LocalDate.of(2025, 1, 4); // 2025-01-04는 토요일
        assertThat(saturday.getDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);

        // when
        boolean result = holidayService.isTradingDay(saturday);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("주말(일요일)은 거래일이 아니다")
    void isTradingDay_Sunday_ReturnsFalse() {
        // given
        LocalDate sunday = LocalDate.of(2025, 1, 5); // 2025-01-05는 일요일
        assertThat(sunday.getDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);

        // when
        boolean result = holidayService.isTradingDay(sunday);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("평일이고 공휴일이 아니면 거래일이다")
    void isTradingDay_WeekdayAndNotHoliday_ReturnsTrue() {
        // given
        LocalDate monday = LocalDate.of(2025, 1, 6); // 2025-01-06은 월요일
        assertThat(monday.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        
        // 공휴일이 없는 월로 설정
        Set<LocalDate> emptyHolidays = new HashSet<>();
        when(holidayApiClient.getHolidaysForMonth(2025, 1)).thenReturn(emptyHolidays);

        // when
        boolean result = holidayService.isTradingDay(monday);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("평일이지만 공휴일이면 거래일이 아니다")
    void isTradingDay_WeekdayButHoliday_ReturnsFalse() {
        // given
        LocalDate newYearDay = LocalDate.of(2025, 1, 1); // 2025-01-01 (수요일, 신정)
        assertThat(newYearDay.getDayOfWeek()).isEqualTo(DayOfWeek.WEDNESDAY);
        
        // 신정을 공휴일로 설정
        Set<LocalDate> holidays = new HashSet<>();
        holidays.add(newYearDay);
        when(holidayApiClient.getHolidaysForMonth(2025, 1)).thenReturn(holidays);

        // when
        boolean result = holidayService.isTradingDay(newYearDay);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("API 호출 실패 시 주말만 체크하여 평일은 거래일로 판단한다")
    void isTradingDay_ApiFailure_ReturnsTrue() {
        // given
        LocalDate monday = LocalDate.of(2025, 1, 6); // 2025-01-06은 월요일
        assertThat(monday.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        
        // API 호출 실패 시뮬레이션
        when(holidayApiClient.getHolidaysForMonth(anyInt(), anyInt())).thenThrow(new RuntimeException("API 호출 실패"));

        // when
        boolean result = holidayService.isTradingDay(monday);

        // then
        assertThat(result).isTrue(); // graceful degradation으로 평일은 거래일로 판단
    }

    @Test
    @DisplayName("월별 공휴일 조회가 정상적으로 작동한다")
    void getHolidaysForMonth_Success() {
        // given
        Set<LocalDate> expectedHolidays = new HashSet<>();
        expectedHolidays.add(LocalDate.of(2025, 1, 1)); // 신정
        expectedHolidays.add(LocalDate.of(2025, 1, 28)); // 설날 연휴 중 하루 (예시)
        
        when(holidayApiClient.getHolidaysForMonth(2025, 1)).thenReturn(expectedHolidays);

        // when
        Set<LocalDate> result = holidayService.getHolidaysForMonth(2025, 1);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).contains(LocalDate.of(2025, 1, 1));
        assertThat(result).contains(LocalDate.of(2025, 1, 28));
    }

    @Test
    @DisplayName("API 호출 실패 시 빈 Set을 반환한다")
    void getHolidaysForMonth_ApiFailure_ReturnsEmptySet() {
        // given
        when(holidayApiClient.getHolidaysForMonth(anyInt(), anyInt())).thenThrow(new RuntimeException("API 호출 실패"));

        // when
        Set<LocalDate> result = holidayService.getHolidaysForMonth(2025, 1);

        // then
        assertThat(result).isEmpty();
    }
}