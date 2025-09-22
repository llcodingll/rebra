package com.rebra.scheduler;

import com.rebra.entity.Portfolio;
import com.rebra.repository.PortfolioRepository;
import com.rebra.service.PerformanceMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 포트폴리오 성과 메트릭 일일 수집 스케줄러
 * 매일 저녁 6시에 모든 포트폴리오의 성과 데이터 수집
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceMetricsScheduler {

    private final PerformanceMetricsService performanceMetricsService;
    private final PortfolioRepository portfolioRepository;

    /**
     * 매일 저녁 6시에 성과 메트릭 수집 실행
     * 평일(월-금)에만 실행
     */
    @Scheduled(cron = "0 0 18 * * MON-FRI", zone = "Asia/Seoul")
    public void collectDailyPerformanceMetrics() {
        if (isMarketDay()) {
            log.info("=== 일일 성과 메트릭 수집 시작 ===");
            executeDailyMetricsCollection();
        }
    }

    /**
     * 성과 메트릭 수집 실행 로직
     */
    private void executeDailyMetricsCollection() {
        try {
            LocalDateTime startTime = LocalDateTime.now();
            LocalDate targetDate = LocalDate.now();

            log.info("성과 메트릭 수집 처리 시작 - 대상 날짜: {}, 시작시간: {}", targetDate, startTime);

            // 모든 포트폴리오 조회
            List<Portfolio> allPortfolios = portfolioRepository.findAll();

            if (allPortfolios.isEmpty()) {
                log.info("수집할 포트폴리오가 없습니다.");
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            // 각 포트폴리오별 성과 메트릭 수집
            for (Portfolio portfolio : allPortfolios) {
                try {
                    // 중복 방지: 이미 오늘 데이터가 존재하는지 확인
                    if (performanceMetricsService.existsMetrics(portfolio.getId(), targetDate)) {
                        log.debug("이미 수집된 성과 메트릭 - Portfolio ID: {}, Date: {}",
                                portfolio.getId(), targetDate);
                        successCount++;
                        continue;
                    }

                    // 성과 메트릭 수집 및 저장
                    performanceMetricsService.collectDailyMetrics(portfolio, targetDate);
                    successCount++;

                    log.debug("성과 메트릭 수집 완료 - Portfolio ID: {}, Name: {}",
                            portfolio.getId(), portfolio.getName());

                } catch (Exception e) {
                    failureCount++;
                    log.error("개별 포트폴리오 성과 메트릭 수집 실패 - Portfolio ID: {}, Name: {}",
                            portfolio.getId(), portfolio.getName(), e);
                    // 개별 포트폴리오 실패가 전체 처리를 중단하지 않도록 continue
                }
            }

            LocalDateTime endTime = LocalDateTime.now();
            long duration = java.time.Duration.between(startTime, endTime).toMillis();

            log.info("성과 메트릭 수집 처리 완료 - 대상 날짜: {}, 종료시간: {}, 소요시간: {}ms, " +
                    "총 포트폴리오: {}, 성공: {}, 실패: {}",
                    targetDate, endTime, duration, allPortfolios.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("성과 메트릭 수집 처리 중 전체 오류 발생", e);
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
            log.debug("주말이므로 성과 메트릭 수집 스케줄 제외: {}", now.getDayOfWeek());
            return false;
        }

        // TODO: 향후 공휴일 API나 설정을 통해 공휴일 체크 로직 추가

        return true;
    }

    /**
     * 수동으로 모든 포트폴리오의 성과 메트릭 수집 실행 (관리자용)
     * 필요시 수동으로 호출할 수 있는 메서드
     */
    public void executeManualMetricsCollection() {
        log.info("=== 수동 전체 포트폴리오 성과 메트릭 수집 실행 ===");
        executeDailyMetricsCollection();
    }

    /**
     * 특정 날짜의 성과 메트릭 수집 실행 (관리자용)
     * 누락된 날짜 데이터를 보완할 때 사용
     */
    public void executeMetricsCollectionForDate(LocalDate targetDate) {
        try {
            log.info("=== 특정 날짜 성과 메트릭 수집 실행 - 날짜: {} ===", targetDate);

            List<Portfolio> allPortfolios = portfolioRepository.findAll();

            if (allPortfolios.isEmpty()) {
                log.info("수집할 포트폴리오가 없습니다.");
                return;
            }

            int successCount = 0;
            int failureCount = 0;

            for (Portfolio portfolio : allPortfolios) {
                try {
                    performanceMetricsService.collectDailyMetrics(portfolio, targetDate);
                    successCount++;

                    log.debug("특정 날짜 성과 메트릭 수집 완료 - Portfolio ID: {}, Date: {}",
                            portfolio.getId(), targetDate);

                } catch (Exception e) {
                    failureCount++;
                    log.error("특정 날짜 성과 메트릭 수집 실패 - Portfolio ID: {}, Date: {}",
                            portfolio.getId(), targetDate, e);
                }
            }

            log.info("특정 날짜 성과 메트릭 수집 완료 - 날짜: {}, 총 포트폴리오: {}, 성공: {}, 실패: {}",
                    targetDate, allPortfolios.size(), successCount, failureCount);

        } catch (Exception e) {
            log.error("특정 날짜 성과 메트릭 수집 중 전체 오류 발생 - 날짜: {}", targetDate, e);
        }
    }
}