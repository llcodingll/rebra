import type { Portfolio, Stock } from '../../../entities/portfolio';
import type { PortfolioItem, PortfolioDetailResponse, RebalancingHistoryItem, ChartDataPoint } from '../api/types';

export const transformPortfolioData = (apiPortfolios: PortfolioItem[]): Portfolio[] => {
  return apiPortfolios.map(item => ({
    id: item.portfolioId.toString(),
    name: item.name,
    description: item.description,
    stockCount: item.registeredStockCount,
    return: `${item.totalReturnRate > 0 ? '+' : ''}${item.totalReturnRate.toFixed(1)}%`,
    createdDate: item.createdAt ? item.createdAt.split('T')[0] : '',
    returnPositive: item.totalReturnRate > 0,
    accountType: item.accountType,
  }));
};

export const transformPortfolioDetailToStocks = (portfolioDetailData: PortfolioDetailResponse): Stock[] => {
  console.log("=== transformPortfolioDetailToStocks 시작 ===");
  console.log("portfolioDetailData:", portfolioDetailData);

  // 등록된 주식들의 총 가중치 계산
  const totalWeight = portfolioDetailData.registeredStocks.reduce((sum, stock) => sum + stock.targetWeight, 0);
  console.log("총 가중치:", totalWeight);

  // 등록된 주식들의 총 현재가 계산
  const totalCurrentValue = portfolioDetailData.registeredStocks.reduce((sum, stock) => sum + (stock.quantity * stock.currentPrice), 0);
  console.log("총 현재가:", totalCurrentValue);

  const registeredStocks: Stock[] = portfolioDetailData.registeredStocks.map(stock => {
    // 목표 비중 = (개별 주식 가중치 / 전체 가중치) * 100
    const calculatedTargetPercentage = totalWeight > 0 ? (stock.targetWeight / totalWeight) * 100 : 0;

    // 현재 비중 = (개별 주식 현재가 / 전체 현재가) * 100
    const currentValue = stock.quantity * stock.currentPrice;
    const calculatedCurrentPercentage = totalCurrentValue > 0 ? (currentValue / totalCurrentValue) * 100 : 0;

    console.log(`${stock.stockName}: 가중치 ${stock.targetWeight} / 전체 ${totalWeight} = ${calculatedTargetPercentage.toFixed(1)}%`);
    console.log(`${stock.stockName}: 현재가 ${currentValue} / 전체 ${totalCurrentValue} = ${calculatedCurrentPercentage.toFixed(1)}%`);

    return {
      id: stock.stockCode,
      name: stock.stockName,
      code: stock.stockCode,
      quantity: stock.quantity,
      averagePrice: stock.purchasePrice,
      currentPrice: stock.currentPrice,
      totalValue: currentValue,
      profitLoss: (stock.currentPrice - stock.purchasePrice) * stock.quantity,
      profitLossRate: ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100,
      type: 'registered' as const,
      targetWeight: stock.targetWeight, // 서버에서 받은 원본 가중치
      targetPercentage: calculatedTargetPercentage, // 계산된 목표 비중
      currentPercentage: calculatedCurrentPercentage, // 계산된 현재 비중
      thresholdPercentage: stock.thresholdPercentage,
    };
  });

  const unregisteredStocks: Stock[] = portfolioDetailData.unregisteredStocks.map(stock => ({
    id: stock.stockCode,
    name: stock.stockName,
    code: stock.stockCode,
    quantity: stock.quantity,
    averagePrice: stock.purchasePrice,
    currentPrice: stock.currentPrice,
    totalValue: stock.quantity * stock.currentPrice,
    profitLoss: (stock.currentPrice - stock.purchasePrice) * stock.quantity,
    profitLossRate: ((stock.currentPrice - stock.purchasePrice) / stock.purchasePrice) * 100,
    type: 'unregistered' as const,
    targetWeight: 0,
    targetPercentage: 0,
    currentPercentage: 0,
    thresholdPercentage: null,
  }));

  const result = [...registeredStocks, ...unregisteredStocks];
  console.log("변환된 stocks:", result);
  console.log("미등록 주식 개수:", unregisteredStocks.length);

  return result;
};

// 리밸런싱 히스토리를 차트 데이터로 변환
export const transformRebalancingHistoryToChart = (historyData: RebalancingHistoryItem[]): ChartDataPoint[] => {
  console.log("=== transformRebalancingHistoryToChart 시작 ===");
  console.log("입력 데이터:", historyData);

  if (!historyData || historyData.length === 0) {
    console.log("히스토리 데이터가 없음");
    return [];
  }

  const chartData = historyData.map((item, index) => {
    const transformedItem = {
      id: item.orderId ?? index, // null인 경우 index를 사용
      date: new Date(item.executedAt).toLocaleDateString('ko-KR', { year: 'numeric', month: 'short' }),
      cumulativeReturn: Number((item.cumulativeReturn - 100).toFixed(2)) // API는 100 기준, 차트는 0 기준, 소수점 2자리
    };

    console.log(`${index}번째 항목:`, {
      원본: item,
      변환결과: transformedItem
    });

    return transformedItem;
  });

  console.log("=== 변환 완료 ===");
  console.log("최종 차트 데이터:", chartData);

  return chartData;
};

// 포트폴리오 퍼센트 데이터 변환
export const transformRebalancingHistoryToPercents = (historyData: RebalancingHistoryItem[]): number[] => {
  console.log("=== transformRebalancingHistoryToPercents 시작 ===");
  console.log("입력 데이터:", historyData);

  if (!historyData || historyData.length === 0) {
    console.log("히스토리 데이터가 없어서 빈 배열 반환");
    return [];
  }

  const percents = historyData.map((item) => item.cumulativeReturn);
  console.log("변환된 퍼센트 데이터:", percents);

  return percents;
};