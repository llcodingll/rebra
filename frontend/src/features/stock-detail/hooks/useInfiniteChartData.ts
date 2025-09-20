import { useInfiniteQuery } from '@tanstack/react-query';
import { stockApi } from '../api/stockApi';
import { getDateRangeByPeriod, getPreviousDateRange, type ChartPeriodType } from '../utils/dateUtils';
import type { StockChartData, ChartDataRequest } from '../api/types';

/**
 * 무한 스크롤 방식의 차트 데이터 조회 훅
 * @param stockCode 종목코드
 * @param period 차트 기간 타입
 * @param enabled 쿼리 활성화 여부
 * @returns TanStack Query Infinite 결과
 */
export const useInfiniteChartData = (
  stockCode: string,
  period: ChartPeriodType = 'daily',
  enabled: boolean = true
) => {
  // 차트 타입별 API 함수 선택
  const getApiFunction = () => {
    switch (period) {
      case 'daily':
        return stockApi.getDailyChartData;
      case 'weekly':
        return stockApi.getWeeklyChartData;
      case 'monthly':
        return stockApi.getMonthlyChartData;
      case 'yearly':
        return stockApi.getYearlyChartData;
      default:
        return stockApi.getDailyChartData;
    }
  };

  return useInfiniteQuery({
    queryKey: ['infiniteStockChart', stockCode, period],
    queryFn: async ({ pageParam }) => {
      let dateRange;

      if (pageParam) {
        // 이전 페이지가 있으면 해당 날짜로부터 과거 데이터 조회
        dateRange = getPreviousDateRange(pageParam, period);
      } else {
        // 첫 페이지는 기본 날짜 범위 사용
        dateRange = getDateRangeByPeriod(period);
      }

      const queryParams: ChartDataRequest = {
        stockCode,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate,
      };

      const result = await getApiFunction()(queryParams);

      if (!result.success) {
        throw new Error(result.error.message || '차트 데이터 조회에 실패했습니다');
      }

      // 다음 페이지를 위한 파라미터는 현재 데이터의 가장 오래된 날짜
      const oldestDate = result.data.chartData && result.data.chartData.length > 0
        ? result.data.chartData.sort((a, b) => a.tradingDate.localeCompare(b.tradingDate))[0].tradingDate
        : dateRange.startDate;

      return {
        data: result.data,
        nextPageParam: oldestDate,
        hasMore: result.data.chartData && result.data.chartData.length > 0
      };
    },
    getNextPageParam: (lastPage) => {
      // 데이터가 없으면 더 이상 로드하지 않음
      if (!lastPage.hasMore) {
        return undefined;
      }
      return lastPage.nextPageParam;
    },
    initialPageParam: undefined as string | undefined,
    enabled: enabled && !!stockCode,
    staleTime: 5 * 60 * 1000, // 5분간 캐시
    gcTime: 10 * 60 * 1000, // 10분간 가비지 컬렉션 대기
  });
};

/**
 * 무한 쿼리의 모든 페이지 데이터를 하나로 병합하는 유틸리티 함수
 * @param pages 무한 쿼리 페이지 배열
 * @returns 병합된 차트 데이터
 */
export const mergeInfiniteChartData = (pages: Array<{ data: StockChartData }> | undefined): StockChartData | null => {
  if (!pages || pages.length === 0) {
    return null;
  }

  const firstPage = pages[0].data;
  if (pages.length === 1) {
    return firstPage;
  }

  // 모든 페이지의 chartData를 병합
  const allChartData = pages
    .map(page => page.data.chartData || [])
    .flat()
    .sort((a, b) => a.tradingDate.localeCompare(b.tradingDate)); // tradingDate로 정렬

  // 중복 제거 (같은 날짜의 데이터가 있을 경우 최신 것 유지)
  const uniqueChartData = allChartData.reduce((acc, current) => {
    const existingIndex = acc.findIndex(item => item.tradingDate === current.tradingDate);
    if (existingIndex >= 0) {
      acc[existingIndex] = current; // 최신 데이터로 덮어쓰기
    } else {
      acc.push(current);
    }
    return acc;
  }, [] as typeof allChartData);

  return {
    ...firstPage,
    chartData: uniqueChartData
  };
};