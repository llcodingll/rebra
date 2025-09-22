package com.rebra.service;

import com.rebra.client.HolidayApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayServiceImpl implements HolidayService {

    private final HolidayApiClient holidayApiClient;

    @Override
    public boolean isTradingDay(LocalDate date) {
        // 주말 체크
        if (date.getDayOfWeek() == DayOfWeek.SATURDAY || 
            date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }
        
        try {
            // 공휴일 체크
            Set<LocalDate> holidays = getHolidaysForMonth(date.getYear(), date.getMonthValue());
            return !holidays.contains(date);
        } catch (Exception e) {
            log.warn("공휴일 API 호출 실패로 주말만 체크함. 날짜: {}, 오류: {}", date, e.getMessage());
            // API 실패 시 graceful degradation - 주말만 체크
            return true;
        }
    }

    /**
     * 특정 연/월의 공휴일 목록을 조회한다
     * 
     * @param year 연도
     * @param month 월
     * @return 공휴일 날짜 Set
     */
    public Set<LocalDate> getHolidaysForMonth(int year, int month) {
        try {
            log.info("공휴일 데이터 조회 시작 - {}년 {}월", year, month);
            Set<LocalDate> holidays = holidayApiClient.getHolidaysForMonth(year, month);
            log.info("공휴일 데이터 조회 완료 - {}년 {}월, 공휴일 수: {}", year, month, holidays.size());
            return holidays;
        } catch (Exception e) {
            log.error("공휴일 데이터 조회 실패 - {}년 {}월", year, month, e);
            // 실패 시 빈 Set 반환 (주말만 체크되도록)
            return new HashSet<>();
        }
    }
}