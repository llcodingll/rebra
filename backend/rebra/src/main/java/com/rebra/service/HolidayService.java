package com.rebra.service;

import java.time.LocalDate;

/**
 * 공휴일 및 거래일 확인 서비스
 */
public interface HolidayService {

    /**
     * 해당 날짜가 거래일인지 판단한다 (주말과 공휴일을 제외한 평일)
     * 
     * @param date 확인할 날짜
     * @return 거래일이면 true, 주말이나 공휴일이면 false
     */
    boolean isTradingDay(LocalDate date);
}