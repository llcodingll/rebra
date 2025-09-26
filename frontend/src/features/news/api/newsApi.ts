import { ApiClient } from '../../../shared/api/apiClient';
import type { Result, AppError } from '../../../shared/util/result';
import type { NewsResponse } from './types';

class NewsApiService extends ApiClient {
  /**
   * 최신 뉴스 목록 조회
   * @returns 최신 뉴스 목록
   */
  getLatestNews = async (): Promise<Result<NewsResponse, AppError>> => {
    return await this.get<NewsResponse>('/api/news/latest');
  };
}

export const newsApi = new NewsApiService();