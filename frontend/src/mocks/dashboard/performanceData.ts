import type { PortfolioPerformanceResponse } from '../../features/portfolio/api/types';

// 포트폴리오 성과 목데이터 생성 함수
export function generatePerformanceMockData(): PortfolioPerformanceResponse {
  const today = new Date();
  const startDate = new Date(today);
  startDate.setDate(today.getDate() - 30); // 30일 전부터 시작

  console.log("히스토리 차트 디버깅");

  const performanceData = [];
  let currentValue = 1000000; // 초기 평가액 100만원

  for (let i = 0; i <= 30; i++) {
    const currentDate = new Date(startDate);
    currentDate.setDate(startDate.getDate() + i);
    const dateString = currentDate.toISOString().split('T')[0];

    // 랜덤하게 평가액 변동 (±5% 범위)
    const change = (Math.random() - 0.5) * 0.1; // -5% ~ +5%
    currentValue = Math.max(currentValue * (1 + change), 500000); // 최소 50만원

    // 이벤트 발생 여부 (확률적으로)
    const hasRebalancing = Math.random() < 0.1; // 10% 확률로 리밸런싱
    const hasCompositionChange = hasRebalancing || Math.random() < 0.05; // 5% 확률로 구성 변경
    const hasBuy = Math.random() < 0.15; // 15% 확률로 매수
    const hasSell = Math.random() < 0.12; // 12% 확률로 매도

    performanceData.push({
      metricDate: dateString,
      totalValue: Math.round(currentValue),
      compositionChanged: hasCompositionChange,
      rebalanced: hasRebalancing,
      sold: hasSell,
      bought: hasBuy,
    });
  }

  const initialValue = performanceData[0].totalValue;
  const finalValue = performanceData[performanceData.length - 1].totalValue;
  const totalReturnRate = ((finalValue - initialValue) / initialValue) * 100;
  const allValues = performanceData.map(d => d.totalValue);
  const maxValue = Math.max(...allValues);
  const minValue = Math.min(...allValues);

  return {
    success: true,
    status: 200,
    data: {
      portfolioId: 1,
      portfolioName: "테스트 포트폴리오",
      portfolioCreatedDate: startDate.toISOString().split('T')[0],
      startDate: startDate.toISOString().split('T')[0],
      endDate: today.toISOString().split('T')[0],
      performanceData: performanceData,
      statistics: {
        totalDataPoints: performanceData.length,
        rebalancingCount: performanceData.filter(d => d.rebalanced).length,
        buyCount: performanceData.filter(d => d.bought).length,
        sellCount: performanceData.filter(d => d.sold).length,
        initialValue: initialValue,
        finalValue: finalValue,
        totalReturnRate: Math.round(totalReturnRate * 100) / 100,
        maxValue: maxValue,
        minValue: minValue,
      }
    },
    timestamp: new Date().toISOString()
  };
}