/**
 * 주식 시장 관련 유틸리티 함수들
 */

/**
 * 서울 시간 기준 주식 시장 시간 체크
 * @returns {boolean} 장이 열려있는지 여부
 */
export const isMarketOpen = (): boolean => {
  const now = new Date();
  const seoulTime = new Date(now.toLocaleString("en-US", {timeZone: "Asia/Seoul"}));

  const day = seoulTime.getDay(); // 0=일요일, 6=토요일
  const hour = seoulTime.getHours();
  const minute = seoulTime.getMinutes();

  // 주말 제외
  if (day === 0 || day === 6) return false;

  // 09:00 ~ 15:30 (서울시간 기준)
  if (hour < 9) return false;
  if (hour > 15) return false;
  if (hour === 15 && minute > 30) return false;

  return true;
};