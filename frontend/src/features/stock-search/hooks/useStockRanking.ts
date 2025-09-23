import { useApi } from '../../../shared/hook/useApi';
import { stockSearchApi } from '../api/stockSearchApi';
import type { StockRankingResponse } from '../api/types';

/**
 * 주식 랭킹 데이터 조회 훅
 * @param refetchInterval 자동 refetch 간격 (기본값: 30초)
 * @returns 랭킹 데이터와 상태
 */
export const useStockRanking = (refetchInterval: number = 30000) => {
  const {
    data: rankingData,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['stockRanking'],
    apiFunction: () => stockSearchApi.getStockRanking(),
    refetchInterval,
    staleTime: 20000, // 20초간 fresh 상태 유지
    gcTime: 5 * 60 * 1000, // 5분간 가비지 컬렉션 대기
  });

  return {
    rankingData: rankingData || [],
    isLoading,
    error,
    refetch,
  };
};