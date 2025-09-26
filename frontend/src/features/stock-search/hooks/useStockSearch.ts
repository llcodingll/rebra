import { useState, useEffect, useMemo } from 'react';
import { useApi } from '../../../shared/hook/useApi';
import { stockSearchApi, transformSearchResults } from '../api/stockSearchApi';
import type { StockSearchRequest, SearchableStock } from '../api/types';

/**
 * 주식 검색 훅
 * @param searchQuery 검색어
 * @param debounceMs 디바운스 시간 (기본값: 300ms)
 * @returns 검색 결과와 상태
 */
export const useStockSearch = (searchQuery: string, debounceMs: number = 300) => {
  const [debouncedQuery, setDebouncedQuery] = useState(searchQuery);
  const [isDebouncing, setIsDebouncing] = useState(false);

  // 디바운싱 처리
  useEffect(() => {
    console.log(`🔍 검색어 변경: "${searchQuery}" → 디바운싱 ${debounceMs}ms 대기`);

    if (searchQuery.trim().length > 0) {
      setIsDebouncing(true);
    }

    const timer = setTimeout(() => {
      console.log(`✅ 디바운싱 완료: "${searchQuery}" → debouncedQuery 업데이트`);
      setDebouncedQuery(searchQuery);
      setIsDebouncing(false);
    }, debounceMs);

    return () => {
      clearTimeout(timer);
      if (searchQuery.trim().length === 0) {
        setIsDebouncing(false);
      }
    };
  }, [searchQuery, debounceMs]);

  // 검색 파라미터
  const searchParams: StockSearchRequest = {
    stockName: debouncedQuery,
  };

  // API 호출 (검색어가 있을 때만)
  const shouldSearch = debouncedQuery.trim().length > 0;
  const hasCurrentQuery = searchQuery.trim().length > 0;

  const {
    data: searchResponse,
    isLoading,
    error,
    refetch,
  } = useApi({
    queryKey: ['stockSearch', debouncedQuery],
    apiFunction: stockSearchApi.searchStocks,
    variables: searchParams,
    enabled: shouldSearch,
    staleTime: 0, // 즉시 stale 처리하여 이전 데이터 표시 방지
    gcTime: 5 * 60 * 1000, // 5분간 가비지 컬렉션 대기
  });

  // 검색 결과를 UI 형태로 변환
  let searchResults: SearchableStock[] = [];

  // 현재 검색어와 디바운싱된 검색어가 일치하는지 확인
  const isQueryMatching = searchQuery.trim() === debouncedQuery.trim();

  console.log(`📊 검색 결과 처리: shouldSearch=${shouldSearch}, hasCurrentQuery=${hasCurrentQuery}, isQueryMatching=${isQueryMatching}, hasResponse=${!!searchResponse}, resultCount=${searchResponse?.length || 0}`);

  // 현재 입력과 디바운싱된 쿼리가 일치하고 검색 결과가 있을 때만 표시
  if (hasCurrentQuery && shouldSearch && searchResponse && isQueryMatching) {
    searchResults = transformSearchResults(searchResponse);
    console.log(`✨ 변환된 검색 결과: ${searchResults.length}개`);
  }

  // 통합 로딩 상태: 디바운싱 중이거나 API 호출 중일 때
  const isSearching = (hasCurrentQuery && isDebouncing) || (shouldSearch && isLoading);

  return {
    searchResults,
    isLoading: isSearching,
    error: shouldSearch ? error : null,
    isEmpty: shouldSearch && !isSearching && searchResults.length === 0 && isQueryMatching,
    hasQuery: hasCurrentQuery,
    debouncedQuery,
    refetch,
  };
};
