import { useApi } from '../../../shared/hook/useApi';
import { stockApi } from '../api/stockApi';
import type { StockDetailResponse } from '../api/types';

/**
 * 주식 보유 정보 조회 훅
 * @param stockCode 종목코드
 * @param enabled 쿼리 활성화 여부
 * @returns TanStack Query 결과
 */
export const useStockDetail = (stockCode: string, enabled: boolean = true) => {
  return useApi<StockDetailResponse, string>({
    queryKey: ['stockDetail', stockCode],
    apiFunction: stockApi.getStockDetail,
    variables: stockCode,
    enabled: enabled && !!stockCode,
    staleTime: 5 * 60 * 1000, // 5분간 캐시
    gcTime: 10 * 60 * 1000, // 10분간 가비지 컬렉션 대기
    errorMessages: {
      404: '해당 종목의 보유 정보를 찾을 수 없습니다',
      500: '보유 정보 조회 중 서버 오류가 발생했습니다',
    },
  });
};
