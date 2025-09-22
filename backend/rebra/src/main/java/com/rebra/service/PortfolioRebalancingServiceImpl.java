package com.rebra.service;

import com.rebra.dto.response.RebalancingExecutionResponse;
import com.rebra.entity.*;
import com.rebra.enums.ExecutionType;
import com.rebra.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioRebalancingServiceImpl implements PortfolioRebalancingService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioRebalancingProcessor processor;
    private final HolidayService holidayService;



    @Override
    @Transactional
    public RebalancingExecutionResponse executeManualRebalancing(Long userId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findByIdAndUserIdWithPortfolioStocks(portfolioId, userId)
                .orElseThrow(() -> new RuntimeException("포트폴리오를 찾을 수 없습니다"));

        log.info("수동 리밸런싱 요청: 포트폴리오 {}", portfolioId);
        
        // 1. RebalancingCalculation 생성
        RebalancingCalculation calculation = processor.calculateRebalancing(portfolio);
        
        // 2. 바로 리밸런싱 실행 (판단 없이)
        RebalancingExecutionResponse result = processor.doRebalancing(calculation, portfolio, ExecutionType.MANUAL);
        
        if (result.isSuccess()) {
            portfolio.updateLastRebalanceDate(LocalDate.now());
            portfolioRepository.save(portfolio);
            log.info("수동 리밸런싱 완료: 포트폴리오 {}", portfolioId);
        } else {
            log.error("수동 리밸런싱 실패: 포트폴리오 {} - {}", portfolioId, result.getFailureReason());
        }
        
        return result;
    }

    @Override
    public void processAllActivePortfolios() {
        LocalDate today = LocalDate.now();
        
        // 거래일 체크
        if (!isTradingDay(today)) {
            log.info("오늘({})은 비거래일입니다. 리밸런싱 처리 건너뜀", today);
            return;
        }
        
        // 오늘 처리할 포트폴리오 조회 (nextRebalanceDate <= today)
        List<Portfolio> pendingPortfolios = portfolioRepository
            .findByAutoRebalancingTrueAndNextRebalanceDateLessThanEqual(today);
        
        log.info("리밸런싱 대상 포트폴리오: {}개", pendingPortfolios.size());
        
        for (Portfolio portfolio : pendingPortfolios) {
            try {
                // 별도 서비스를 통해 호출 → 프록시 거침 → 트랜잭션 적용됨
                processor.processPortfolio(portfolio.getId(), today);
            } catch (Exception e) {
                log.error("포트폴리오 {} 리밸런싱 실패", portfolio.getId(), e);
            }
        }
    }







    /**
     * 해당 날짜가 거래일인지 판단한다
     * 
     * @param date 확인할 날짜
     * @return 거래일이면 true
     */
    private boolean isTradingDay(LocalDate date) {
        return holidayService.isTradingDay(date);
    }

}