import { useQuery } from '@tanstack/react-query';
import { stockApi } from '../api/stockApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import type { OptimizedOrderbookData } from '../api/types';

interface UseOrderBookFallbackOptions {
  enabled?: boolean;
  staleTime?: number;
  cacheTime?: number;
}

/**
 * 호가 데이터 폴백 훅
 * REST API를 사용해 호가 데이터를 미리 로드하고, 웹소켓 데이터와 조합하여 안정적인 호가 표시
 *
 * @param stockCode 종목 코드
 * @param wsOrderbook 웹소켓에서 받은 호가 데이터
 * @param options 옵션
 * @returns 최종 호가 데이터 (웹소켓 우선, REST API 폴백)
 */
export function useOrderBookFallback(
  stockCode: string,
  wsOrderbook: OptimizedOrderbookData | null,
  options: UseOrderBookFallbackOptions = {}
) {
  const { accountId } = useAccountStore();
  const {
    enabled = true,
    staleTime = 10000, // 10초
    // cacheTime = 30000, // 30초
  } = options;

  // REST API 호가 데이터 조회
  const {
    data: restOrderbook,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ['orderbook-fallback', stockCode, accountId],
    queryFn: async () => {
      if (!accountId) {
        throw new Error('계좌 ID가 필요합니다');
      }

      const result = await stockApi.getCurrentQuotes({
        stockCode,
        accountId,
      });

      if (!result.success) {
        throw new Error(result.error?.message || '호가 데이터 조회 실패');
      }

      return result.data;
    },
    enabled: enabled && !!stockCode && !!accountId,
    staleTime,
    retry: 2,
    retryDelay: (attemptIndex) => Math.min(1000 * 2 ** attemptIndex, 30000),
  });

  // 웹소켓 데이터 우선, REST API 데이터 폴백
  const finalOrderbook = wsOrderbook || restOrderbook || null;

  // 데이터 소스 정보
  const dataSource: 'websocket' | 'rest' | 'none' = wsOrderbook ? 'websocket' : restOrderbook ? 'rest' : 'none';

  return {
    orderbook: finalOrderbook,
    dataSource,
    isLoading,
    error,
    refetch,
    // 디버깅용 정보
    wsOrderbook,
    restOrderbook,
  };
}
