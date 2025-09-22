package com.rebra.scheduler;

import com.rebra.service.PortfolioRebalancingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 포트폴리오 자동 리밸런싱 스케줄러
 * 매일 오후 2시에 리밸런싱 체크 및 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PortfolioRebalancingScheduler {

    private final PortfolioRebalancingService portfolioRebalancingService;

    /**
     * 오후 2시에 자동 리밸런싱 체크 및 실행
     * 하루 한 번 리밸런싱 처리
     */
    @Scheduled(cron = "0 0 14 * * MON-FRI", zone = "Asia/Seoul")
    public void executeDailyRebalancing() {
        if (isMarketDay()) {
            log.info("=== 일일 자동 리밸런싱 체크 시작 ===");
            executeRebalancing("일일 체크");
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
     * 수동으로 모든 포트폴리오 리밸런싱 실행 (관리자용)
     * 필요시 수동으로 호출할 수 있는 메서드
     */
    public void executeManualRebalancingForAll() {
        log.info("=== 수동 전체 포트폴리오 리밸런싱 실행 ===");
        executeRebalancing("수동 실행");
    }
}