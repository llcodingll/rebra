package com.rebra.scheduler;

import com.rebra.service.PortfolioRebalancingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 포트폴리오 자동 리밸런싱 스케줄러
 * 주식 시장 개장 시간(오전 9시)과 마감 전(오후 3시 20분)에 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioRebalancingScheduler {

    private final PortfolioRebalancingService portfolioRebalancingService;

    /**
     * 오전 9시 5분에 자동 리밸런싱 체크 및 실행
     * 장 시작 직후 실행하여 하루 종일 임계값 기반 리밸런싱 기회를 제공
     */
    @Scheduled(cron = "0 5 9 * * MON-FRI", zone = "Asia/Seoul")
    public void executeMarketOpenRebalancing() {
        if (isMarketDay()) {
            log.info("=== 장 시작 자동 리밸런싱 체크 시작 ===");
            executeRebalancing("장 시작");
        }
    }

    /**
     * 오후 3시 20분에 자동 리밸런싱 체크 및 실행
     * 장 마감 전 마지막 리밸런싱 기회
     */
    @Scheduled(cron = "0 20 15 * * MON-FRI", zone = "Asia/Seoul")
    public void executeMarketCloseRebalancing() {
        if (isMarketDay()) {
            log.info("=== 장 마감 전 자동 리밸런싱 체크 시작 ===");
            executeRebalancing("장 마감 전");
        }
    }

    /**
     * 매시간 정각에 자동 리밸런싱 체크 및 실행 (장 중에만)
     * 임계값 기반 리밸런싱을 위한 정기적인 체크
     */
    @Scheduled(cron = "0 0 9-14 * * MON-FRI", zone = "Asia/Seoul")
    public void executeHourlyRebalancing() {
        if (isMarketDay() && isMarketHours()) {
            log.info("=== 정시 자동 리밸런싱 체크 시작 ===");
            executeRebalancing("정시 체크");
        }
    }

    /**
     * 리밸런싱 실행 로직
     */
    private void executeRebalancing(String trigger) {
        try {
            LocalDateTime startTime = LocalDateTime.now();
            log.info("자동 리밸런싱 처리 시작 - 트리거: {}, 시작시간: {}", trigger, startTime);
            
            portfolioRebalancingService.processAllActivePortfolios();
            
            LocalDateTime endTime = LocalDateTime.now();
            log.info("자동 리밸런싱 처리 완료 - 트리거: {}, 종료시간: {}, 소요시간: {}ms", 
                    trigger, endTime, 
                    java.time.Duration.between(startTime, endTime).toMillis());
                    
        } catch (Exception e) {
            log.error("자동 리밸런싱 처리 중 오류 발생 - 트리거: {}", trigger, e);
        }
    }

    /**
     * 주식 시장 거래일 여부 확인
     * 현재는 평일만 체크하지만, 향후 공휴일 로직 추가 가능
     */
    private boolean isMarketDay() {
        LocalDateTime now = LocalDateTime.now();
        int dayOfWeek = now.getDayOfWeek().getValue();
        
        // 월(1)~금(5)만 거래일로 판단
        boolean isWeekday = dayOfWeek >= 1 && dayOfWeek <= 5;
        
        if (!isWeekday) {
            log.debug("주말이므로 리밸런싱 스케줄 제외: {}", now.getDayOfWeek());
            return false;
        }
        
        // TODO: 향후 공휴일 API나 설정을 통해 공휴일 체크 로직 추가
        
        return true;
    }

    /**
     * 주식 시장 거래 시간 여부 확인 (오전 9시 ~ 오후 3시 30분)
     */
    private boolean isMarketHours() {
        LocalTime now = LocalTime.now();
        LocalTime marketOpen = LocalTime.of(9, 0);    // 09:00
        LocalTime marketClose = LocalTime.of(15, 30); // 15:30
        
        boolean isMarketTime = !now.isBefore(marketOpen) && !now.isAfter(marketClose);
        
        if (!isMarketTime) {
            log.debug("장외 시간이므로 리밸런싱 제외: {}", now);
        }
        
        return isMarketTime;
    }

    /**
     * 수동으로 모든 포트폴리오 리밸런싱 실행 (관리자용)
     * 필요시 수동으로 호출할 수 있는 메서드
     */
    public void executeManualRebalancingForAll() {
        log.info("=== 수동 전체 포트폴리오 리밸런싱 실행 ===");
        executeRebalancing("수동 실행");
    }
}