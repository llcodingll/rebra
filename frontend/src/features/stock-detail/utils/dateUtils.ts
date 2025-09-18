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
 * 차트 타입별 날짜 범위를 구함
 */
export type ChartPeriodType = 'daily' | 'weekly' | 'monthly' | 'yearly';

/**
 * 현재일자에서 4개월 전까지의 날짜 범위를 구함 (일봉용)
 */
export const getDefaultDateRange = (): { startDate: string; endDate: string } => {
  return getDateRangeByPeriod('daily');
};

/**
 * 차트 타입별 날짜 범위 계산
 */
export const getDateRangeByPeriod = (period: ChartPeriodType): { startDate: string; endDate: string } => {
  const today = new Date();
  const endDate = formatDateToString(today);

  const start = new Date(today);

  switch (period) {
    case 'daily':
      // 일봉: 최근 4개월
      start.setMonth(today.getMonth() - 4);
      break;
    case 'weekly':
      // 주봉: 최근 2년
      start.setFullYear(today.getFullYear() - 2);
      break;
    case 'monthly':
      // 월봉: 최근 8년
      start.setFullYear(today.getFullYear() - 8);
      break;
    case 'yearly':
      // 연봉: 최근 100년
      start.setFullYear(today.getFullYear() - 100);
      break;
    default:
      // 기본값: 4개월
      start.setMonth(today.getMonth() - 4);
  }

  const startDate = formatDateToString(start);
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
