import { useApi } from '../../../shared/hook/useApi';
import { stockApi } from '../api/stockApi';
import { getDateRangeByPeriod, type ChartPeriodType } from '../utils/dateUtils';
import type { StockChartData, ChartDataRequest } from '../api/types';

/**
 * 차트 데이터 조회 훅 (일봉/주봉/월봉/연봉)
 * @param stockCode 종목코드
 * @param period 차트 기간 타입
 * @param enabled 쿼리 활성화 여부
 * @returns TanStack Query 결과
 */
export const useStockChartData = (
  stockCode: string,
  period: ChartPeriodType = 'daily',
  enabled: boolean = true
) => {
  const { startDate, endDate } = getDateRangeByPeriod(period);

  const queryParams: ChartDataRequest = {
    stockCode,
    startDate,
    endDate,
  };

  // 차트 타입별 API 함수 선택
  const getApiFunction = () => {
    switch (period) {
      case 'daily':
        return stockApi.getDailyChartData.bind(stockApi);
      case 'weekly':
        return stockApi.getWeeklyChartData.bind(stockApi);
      case 'monthly':
        return stockApi.getMonthlyChartData.bind(stockApi);
      case 'yearly':
        return stockApi.getYearlyChartData.bind(stockApi);
      default:
        return stockApi.getDailyChartData.bind(stockApi);
    }
  };

  return useApi<StockChartData, ChartDataRequest>({
    queryKey: ['stockChart', stockCode, period, startDate, endDate],
    apiFunction: getApiFunction(),
    variables: queryParams,
    enabled: enabled && !!stockCode,
    staleTime: 5 * 60 * 1000, // 5분간 캐시
    gcTime: 10 * 60 * 1000, // 10분간 가비지 컬렉션 대기
  });
};