import { useMemo, useRef, useEffect } from 'react';
import { useApi } from '../../../shared/hook/useApi';
import { stockApi } from '../api/stockApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import { calculateKISFeesAndTaxes, floorPrice } from '../../../shared/util/calculation';
import type { StockHoldingApiResponse, StockHoldingData } from '../api/types';

/**
 * 특정 종목의 보유 정보 조회 및 실시간 계산 훅
 * @param stockCode 종목코드
 * @param currentPrice 현재가 (실시간)
 * @param enabled 쿼리 활성화 여부
 * @returns 보유 정보와 실시간 계산된 데이터
 */
export const useStockHolding = (stockCode: string, currentPrice: number = 0, enabled: boolean = true) => {
  const { accountId } = useAccountStore();
  const previousHoldingResponse = useRef<StockHoldingApiResponse | null>(null);

  // API 호출 (accountId가 있을 때만) - currentPrice와 무관하게 한번만 실행
  const shouldFetch = enabled && !!stockCode && accountId !== null;

  const {
    data: holdingResponse,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['stockHolding', stockCode, accountId], // currentPrice 의존성 제거
    apiFunction: stockApi.getStockHolding,
    variables: { stockCode, accountId: accountId! },
    enabled: shouldFetch,
    staleTime: 0, // 5분간 fresh 상태 유지 (보유 정보는 자주 변하지 않음)
    gcTime: 10 * 60 * 1000, // 10분간 가비지 컬렉션 대기
    // 서버에서 null 반환하는 경우를 정상으로 처리
    initialData: null, // 초기값으로 null 설정
    errorMessages: {
      404: '해당 종목의 보유 정보를 찾을 수 없습니다',
      500: '보유 정보 조회 중 서버 오류가 발생했습니다',
    },
  });

  // holdingResponse가 유효할 때 previousHoldingResponse 업데이트
  useEffect(() => {
    if (holdingResponse) {
      previousHoldingResponse.current = holdingResponse;
    }
  }, [holdingResponse]);

  // 실시간 계산된 보유 정보 - API 응답과 currentPrice만으로 계산
  const holdingData: StockHoldingData | null = useMemo(() => {
    // 현재 응답 또는 이전에 성공한 응답 사용
    const dataToUse = holdingResponse || previousHoldingResponse.current;

    // 사용할 데이터가 없으면 null 반환
    if (!dataToUse) return null;

    const evaluationAmount = currentPrice * dataToUse.holdingQuantity;
    const evaluationProfitLoss = evaluationAmount - dataToUse.purchaseAmount;
    const returnRate = dataToUse.purchaseAmount > 0 ? (evaluationProfitLoss / dataToUse.purchaseAmount) * 100 : 0;

    // 수수료/세금 계산
    const { fee, tax } = calculateKISFeesAndTaxes(evaluationAmount);

    return {
      averagePurchasePrice: floorPrice(dataToUse.averagePurchasePrice), // 평균단가 소수점 버림
      purchaseAmount: dataToUse.purchaseAmount,
      holdingQuantity: dataToUse.holdingQuantity,
      orderableQuantity: dataToUse.orderableQuantity,
      evaluationAmount,
      evaluationProfitLoss,
      returnRate,
      fee,
      tax,
    };
  }, [holdingResponse, currentPrice]); // previousHoldingResponse.current 의존성 제거

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
