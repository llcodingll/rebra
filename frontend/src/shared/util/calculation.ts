/**
 * 주식 관련 공통 계산 유틸리티 함수들
 */

/**
 * 한국투자증권 기준 수수료 및 세금 계산
 * @param evaluationAmount 평가금액
 * @returns 수수료와 세금 객체
 */
export const calculateKISFeesAndTaxes = (evaluationAmount: number) => {
  // 매매수수료: 0.0145% (소수점 버림)
  const fee = Math.floor(evaluationAmount * 0.000145);

  // 증권거래세: 0.23% (소수점 버림)
  const tax = Math.floor(evaluationAmount * 0.0023);

  return { fee, tax };
};

/**
 * 가격 소수점 버림 처리
 * @param price 원본 가격
 * @returns 소수점 버린 가격
 */
export const floorPrice = (price: number): number => {
  return Math.floor(price);
};