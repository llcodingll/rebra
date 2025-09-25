import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useApi } from '../../../shared/hook/useApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import { stockSearchApi } from '../api/stockSearchApi';
import type { WatchlistResponse, WatchlistToggleResponse } from '../api/types';

/**
 * 관심종목 목록 조회 훅
 * @returns 관심종목 목록과 관련 상태
 */
export const useWatchlist = () => {
  const { accountId } = useAccountStore();

  return useApi<WatchlistResponse, number>({
    queryKey: ['watchlist', accountId],
    apiFunction: stockSearchApi.getWatchlist,
    variables: accountId!,
    enabled: !!accountId,
    errorMessages: {
      404: '관심종목을 찾을 수 없습니다',
      500: '관심종목 조회 중 오류가 발생했습니다'
    },
    staleTime: 5 * 60 * 1000, // 5분간 fresh 상태 유지
  });
};

/**
 * 관심종목 토글 (추가/제거) 훅
 * @returns 관심종목 토글 mutation과 관련 상태
 */
export const useToggleWatchlist = () => {
  const { accountId } = useAccountStore();
  const queryClient = useQueryClient();

  return useMutation<WatchlistToggleResponse, Error, string>({
    mutationFn: async (stockCode: string) => {
      if (!accountId) {
        throw new Error('계정 정보가 없습니다');
      }

      const result = await stockSearchApi.toggleWatchlist({ stockCode, accountId });

      if (result.success) {
        return result.data;
      } else {
        throw new Error(result.error.message);
      }
    },
    onSuccess: (data) => {
      // 관심종목 목록 다시 조회
      queryClient.invalidateQueries({ queryKey: ['watchlist'] });

      // 성공 메시지 (선택사항)
      console.log(data.message);
    },
    onError: (error) => {
      console.error('관심종목 토글 실패:', error.message);
    }
  });
};

/**
 * 특정 종목이 관심종목인지 확인하는 헬퍼 함수
 * @param stockCode 종목 코드
 * @param watchlistData 관심종목 목록 데이터
 * @returns 관심종목 여부
 */
export const isWatchlistStock = (stockCode: string, watchlistData?: WatchlistResponse): boolean => {
  if (!watchlistData) return false;
  return watchlistData.some(item => item.stockCode === stockCode);
};