import { useApi } from '../../../shared/hook/useApi';
import { stockSearchApi, transformHoldingsResults } from '../api/stockSearchApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import type { HoldingsRequest, DisplayHoldingStock } from '../api/types';

/**
 * 보유 종목 조회 훅
 * @param page 페이지 번호
 * @param size 페이지 크기 (기본값: 5)
 * @returns 보유 종목 데이터와 상태
 */
export const useHoldings = (page: number = 0, size: number = 5) => {
  const { accountId } = useAccountStore();

  // API 호출 (accountId가 있을 때만)
  const shouldFetch = accountId !== null;

  const searchParams: HoldingsRequest = {
    accountId: accountId || 0,
    page,
    size,
  };

  const {
    data: holdingsResponse,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['holdings', accountId, page, size],
    apiFunction: stockSearchApi.getHoldings,
    variables: searchParams,
    enabled: shouldFetch,
    refetchInterval: 1000,
    staleTime: 30 * 1000, // 30초간 fresh 상태 유지
    gcTime: 5 * 60 * 1000, // 5분간 가비지 컬렉션 대기
  });

  // 보유 종목을 UI 형태로 변환 (수수료/세금 계산 포함)
  let holdings: DisplayHoldingStock[] = [];

  if (holdingsResponse) {
    holdings = transformHoldingsResults(holdingsResponse);
  }

  return {
    holdings,
    isLoading: shouldFetch ? isLoading : false,
    error: shouldFetch ? error : null,
    isEmpty: shouldFetch && !isLoading && holdings.length === 0,
    hasAccount: accountId !== null,
    refetch,
  };
};
