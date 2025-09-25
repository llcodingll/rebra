import { useApi } from '../../../shared/hook/useApi';
import { newsApi } from '../api/newsApi';
import type { NewsResponse } from '../api/types';

/**
 * 최신 뉴스 목록 조회 훅
 * @returns 뉴스 목록과 관련 상태
 */
export const useLatestNews = () => {
  return useApi<NewsResponse, void>({
    queryKey: ['news', 'latest'],
    apiFunction: newsApi.getLatestNews,
    errorMessages: {
      404: '뉴스를 찾을 수 없습니다',
      500: '뉴스 조회 중 오류가 발생했습니다',
    },
    staleTime: 0, //5 * 60 * 1000, // 5분간 fresh 상태 유지
  });
};
