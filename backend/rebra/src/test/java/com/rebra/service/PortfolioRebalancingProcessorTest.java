package com.rebra.service;

import com.rebra.entity.Portfolio;
import com.rebra.entity.RebalancingPeriod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioRebalancingProcessorTest {

    @InjectMocks
    private PortfolioRebalancingProcessor processor;

    @Test
    void 월간_리밸런싱_다음_날짜_계산_정상케이스() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.MONTHLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2024, 1, 31));
        when(portfolio.getNextRebalanceDate()).thenReturn(LocalDate.of(2024, 3, 31));
        
        LocalDate baseDate = LocalDate.of(2024, 3, 31); // 실제 실행일
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2024, 4, 30), nextDate); // 4월 31일 → 4월 30일로 조정
    }
    
    @Test
    void 월간_리밸런싱_공휴일_지연_시나리오() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.MONTHLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2024, 1, 31));
        when(portfolio.getNextRebalanceDate()).thenReturn(LocalDate.of(2024, 3, 31)); // 원래 예정일
        
        LocalDate baseDate = LocalDate.of(2024, 4, 1); // 공휴일로 인해 4월 1일에 실행됨
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2024, 4, 30), nextDate); // 여전히 4월 30일 (5월 1일이 아닌)
    }
    
    @Test
    void 월간_리밸런싱_2월_윤년_처리() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.MONTHLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2024, 1, 31));
        when(portfolio.getNextRebalanceDate()).thenReturn(LocalDate.of(2024, 1, 31));
        
        LocalDate baseDate = LocalDate.of(2024, 1, 31);
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2024, 2, 29), nextDate); // 윤년 2월 29일
    }
    
    @Test
    void 월간_리밸런싱_평년_2월_처리() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.MONTHLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2023, 1, 31));
        when(portfolio.getNextRebalanceDate()).thenReturn(LocalDate.of(2023, 1, 31));
        
        LocalDate baseDate = LocalDate.of(2023, 1, 31);
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2023, 2, 28), nextDate); // 평년 2월 28일
    }
    
    @Test
    void 월간_리밸런싱_최초_설정시() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.MONTHLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2024, 1, 31));
        when(portfolio.getNextRebalanceDate()).thenReturn(null); // 최초 설정
        
        LocalDate baseDate = LocalDate.of(2024, 1, 31);
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2024, 2, 29), nextDate); // startDate 기준으로 계산
    }
    
    @Test
    void 연간_리밸런싱_윤년_처리() {
        // given
        Portfolio portfolio = mock(Portfolio.class);
        when(portfolio.getRebalancingPeriod()).thenReturn(RebalancingPeriod.YEARLY);
        when(portfolio.getRebalancingStartDate()).thenReturn(LocalDate.of(2024, 2, 29)); // 윤년 2월 29일
        when(portfolio.getNextRebalanceDate()).thenReturn(LocalDate.of(2024, 2, 29));
        
        LocalDate baseDate = LocalDate.of(2024, 2, 29);
        
        // when
        LocalDate nextDate = processor.calculateNextPeriodicDate(portfolio, baseDate);
        
        // then
        assertEquals(LocalDate.of(2025, 2, 28), nextDate); // 평년으로 조정
    }
}