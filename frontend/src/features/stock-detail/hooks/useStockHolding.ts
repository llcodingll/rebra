import { useMemo } from 'react';
import { useApi } from '../../../shared/hook/useApi';
import { stockApi } from '../api/stockApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import type { StockHoldingApiResponse, StockHoldingData } from '../api/types';

/**
 * 특정 종목의 보유 정보 조회 및 실시간 계산 훅
 * @param stockCode 종목코드
 * @param currentPrice 현재가 (실시간)
 * @param enabled 쿼리 활성화 여부
 * @returns 보유 정보와 실시간 계산된 데이터
 */
export const useStockHolding = (
  stockCode: string,
  currentPrice: number = 0,
  enabled: boolean = true
) => {
  const { accountId } = useAccountStore();

  // API 호출 (accountId가 있을 때만)
  const shouldFetch = enabled && !!stockCode && accountId !== null;

  const {
    data: holdingResponse,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['stockHolding', stockCode, accountId],
    apiFunction: stockApi.getStockHolding,
    variables: { stockCode, accountId: accountId! },
    enabled: shouldFetch,
    staleTime: 30 * 1000, // 30초간 fresh 상태 유지
    gcTime: 5 * 60 * 1000, // 5분간 가비지 컬렉션 대기
    errorMessages: {
      404: '해당 종목의 보유 정보를 찾을 수 없습니다',
      500: '보유 정보 조회 중 서버 오류가 발생했습니다',
    },
  });

  // 실시간 계산된 보유 정보
  const holdingData: StockHoldingData | null = useMemo(() => {
    if (!holdingResponse) return null;

    const evaluationAmount = currentPrice * holdingResponse.holdingQuantity;
    const evaluationProfitLoss = evaluationAmount - holdingResponse.purchaseAmount;
    const returnRate = holdingResponse.purchaseAmount > 0
      ? (evaluationProfitLoss / holdingResponse.purchaseAmount) * 100
      : 0;

    return {
      averagePurchasePrice: holdingResponse.averagePurchasePrice,
      purchaseAmount: holdingResponse.purchaseAmount,
      holdingQuantity: holdingResponse.holdingQuantity,
      orderableQuantity: holdingResponse.orderableQuantity,
      evaluationAmount,
      evaluationProfitLoss,
      returnRate,
    };
  }, [holdingResponse, currentPrice]);

  return {
    holdingData,
    rawData: holdingResponse,
    isLoading: shouldFetch ? isLoading : false,
    error: shouldFetch ? error : null,
    hasHolding: !!holdingResponse,
    hasAccount: accountId !== null,
    refetch,
  };
};