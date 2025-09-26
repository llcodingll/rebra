/**
 * 뉴스 API 관련 타입 정의
 */

// 뉴스 아이템 타입
export interface NewsItem {
  id: number;
  title: string;
  summary: string;
  url: string;
  imageUrl: string;
  publishedAt: string;
}

// 뉴스 API 응답 타입
export interface NewsResponse {
  news: NewsItem[];
  totalCount: number;
}