import type {
  KRXRealtimePriceMessage,
  KRXRealtimeOrderbookMessage,
  RealtimePriceMessage,
  RealtimeOrderbookMessage,
  OrderBookItem
} from '../api/types';

/**
 * KRX 실시간 체결가 데이터를 기존 인터페이스로 변환
 */
export const transformKRXPriceData = (krxData: KRXRealtimePriceMessage): RealtimePriceMessage => {
  return {
    stockCode: krxData.MKSC_SHRN_ISCD,
    currentPrice: krxData.STCK_PRPR,
    change: krxData.PRDY_VRSS,
    changePercent: krxData.PRDY_CTRT,
    volume: krxData.ACML_VOL,
    timestamp: krxData.STCK_CNTG_HOUR,
  };
};

/**
 * KRX 실시간 호가 데이터를 기존 인터페이스로 변환
 */
export const transformKRXOrderbookData = (krxData: KRXRealtimeOrderbookMessage): RealtimeOrderbookMessage => {
  // 매도호가 (asks) - 높은 가격부터 내림차순
  const asks: OrderBookItem[] = [
    { price: krxData.ASKP10, quantity: krxData.ASKP_RSQN10, size: 0 },
    { price: krxData.ASKP9, quantity: krxData.ASKP_RSQN9, size: 0 },
    { price: krxData.ASKP8, quantity: krxData.ASKP_RSQN8, size: 0 },
    { price: krxData.ASKP7, quantity: krxData.ASKP_RSQN7, size: 0 },
    { price: krxData.ASKP6, quantity: krxData.ASKP_RSQN6, size: 0 },
    { price: krxData.ASKP5, quantity: krxData.ASKP_RSQN5, size: 0 },
    { price: krxData.ASKP4, quantity: krxData.ASKP_RSQN4, size: 0 },
    { price: krxData.ASKP3, quantity: krxData.ASKP_RSQN3, size: 0 },
    { price: krxData.ASKP2, quantity: krxData.ASKP_RSQN2, size: 0 },
    { price: krxData.ASKP1, quantity: krxData.ASKP_RSQN1, size: 0 },
  ].filter(item => item.price > 0); // 0인 호가 제거

  // 매수호가 (bids) - 높은 가격부터 내림차순
  const bids: OrderBookItem[] = [
    { price: krxData.BIDP1, quantity: krxData.BIDP_RSQN1, size: 0 },
    { price: krxData.BIDP2, quantity: krxData.BIDP_RSQN2, size: 0 },
    { price: krxData.BIDP3, quantity: krxData.BIDP_RSQN3, size: 0 },
    { price: krxData.BIDP4, quantity: krxData.BIDP_RSQN4, size: 0 },
    { price: krxData.BIDP5, quantity: krxData.BIDP_RSQN5, size: 0 },
    { price: krxData.BIDP6, quantity: krxData.BIDP_RSQN6, size: 0 },
    { price: krxData.BIDP7, quantity: krxData.BIDP_RSQN7, size: 0 },
    { price: krxData.BIDP8, quantity: krxData.BIDP_RSQN8, size: 0 },
    { price: krxData.BIDP9, quantity: krxData.BIDP_RSQN9, size: 0 },
    { price: krxData.BIDP10, quantity: krxData.BIDP_RSQN10, size: 0 },
  ].filter(item => item.price > 0); // 0인 호가 제거

  return {
    stockCode: krxData.MKSC_SHRN_ISCD,
    asks,
    bids,
    timestamp: krxData.BSOP_HOUR,
  };
};