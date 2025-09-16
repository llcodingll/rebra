import { useApi } from '../../../shared/hook/useApi';
import { stockApi } from '../api/stockApi';
import { getDefaultDateRange } from '../utils/dateUtils';
import type { DailyChartData, ChartDataRequest } from '../api/types';

/**
 * 일봉 차트 데이터 조회 훅
 * @param stockCode 종목코드
 * @param enabled 쿼리 활성화 여부
 * @returns TanStack Query 결과
 */
export const useStockChartData = (stockCode: string, enabled: boolean = true) => {
  const { startDate, endDate } = getDefaultDateRange();

  const queryParams: ChartDataRequest = {
    stockCode,
    startDate,
    endDate,
  };

  return useApi<DailyChartData, ChartDataRequest>({
    queryKey: ['stockChart', stockCode, startDate, endDate],
    apiFunction: stockApi.getDailyChartData.bind(stockApi),
    variables: queryParams,
    enabled: enabled && !!stockCode,
    staleTime: 5 * 60 * 1000, // 5분간 캐시
    gcTime: 10 * 60 * 1000, // 10분간 가비지 컬렉션 대기
  });
};