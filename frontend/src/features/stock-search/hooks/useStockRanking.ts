import { useApi } from '../../../shared/hook/useApi';
import { stockSearchApi } from '../api/stockSearchApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import type { VolumeRankingApiResponse } from '../api/types';

export type RankingType = 'volume' | 'rising' | 'falling';

/**
 * 주식 랭킹 데이터 조회 훅
 * @param rankingType 랭킹 타입 ('volume' | 'rising' | 'falling')
 * @param refetchInterval 자동 refetch 간격 (기본값: 1초)
 * @returns 랭킹 데이터와 상태
 */
export const useStockRanking = (rankingType: RankingType, refetchInterval: number = 100000) => {
  const { accountId } = useAccountStore();

  // 랭킹 타입에 따른 API 함수 선택
  const getApiFunction = () => {
    switch (rankingType) {
      case 'volume':
        return () => stockSearchApi.getStockRanking(accountId!);
      case 'rising':
        return () => stockSearchApi.getRisingStockRanking(accountId!);
      case 'falling':
        return () => stockSearchApi.getFallingStockRanking(accountId!);
      default:
        return () => stockSearchApi.getStockRanking(accountId!);
    }
  };

  const {
    data: rankingData,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['stockRanking', rankingType, accountId],
    apiFunction: getApiFunction(),
    enabled: accountId !== null,
    refetchInterval,
    staleTime: 0, // 항상 stale 상태로 간주
    gcTime: 5 * 60 * 1000, // 5분간 가비지 컬렉션 대기
    // onSuccess: (data) => {
    //   console.log('📊 랭킹 데이터 refetch:', rankingType, data?.rankings?.length, '개');
    // },
  });

  console.log('🔄 useStockRanking 훅 실행:', rankingType);

  return {
    rankingData: rankingData?.rankings || [],
    isLoading,
    error,
    refetch,
  };
};
