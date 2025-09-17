import type { UTCTimestamp } from 'lightweight-charts';

/**
 * 주식 차트 데이터 조회를 위한 날짜 계산 유틸리티
 */

/**
 * 날짜를 YYYYMMDD 형식 문자열로 변환
 */
export const formatDateToString = (date: Date): string => {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}${month}${day}`;
};

/**
 * 현재일자-1에서 3개월 전까지의 날짜 범위를 구함
 */
export const getDefaultDateRange = (): { startDate: string; endDate: string } => {
  const today = new Date();

  // 전날 (어제)
  const yesterday = new Date(today);
  yesterday.setDate(today.getDate() - 1);
  const endDate = formatDateToString(yesterday);

  // 3개월 전
  const threeMonthsAgo = new Date(yesterday);
  threeMonthsAgo.setMonth(yesterday.getMonth() - 3);
  const startDate = formatDateToString(threeMonthsAgo);

  return { startDate, endDate };
};

/**
 * TradingView 차트용 UTCTimestamp 변환
 * YYYYMMDD 문자열을 UTC 타임스탬프로 변환
 */
export const dateStringToTimestamp = (dateString: string): UTCTimestamp => {
  const year = parseInt(dateString.substring(0, 4));
  const month = parseInt(dateString.substring(4, 6)) - 1;
  const day = parseInt(dateString.substring(6, 8));
  const date = new Date(year, month, day, 9, 0, 0, 0); // 장 시작 시간 09:00
  return Math.floor(date.getTime() / 1000) as UTCTimestamp;
};