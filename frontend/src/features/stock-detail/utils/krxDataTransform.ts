import type {
  OptimizedPriceData,
  OptimizedOrderbookData,
  RealtimePriceMessage,
  RealtimeOrderbookMessage,
  OrderBookItem
} from '../api/types';

/**
 * 최적화된 실시간 체결가 데이터를 기존 인터페이스로 변환
 */
export const transformOptimizedPriceData = (optimizedData: OptimizedPriceData): RealtimePriceMessage => {
  return {
    stockCode: optimizedData.stockCode,
    currentPrice: parseFloat(optimizedData.stckPrpr),
    change: parseFloat(optimizedData.prdyVrss),
    changePercent: parseFloat(optimizedData.prdyCtrt),
    volume: parseFloat(optimizedData.acmlVol),
    timestamp: optimizedData.timestamp.toString(),
  };
};

/**
 * 최적화된 실시간 호가 데이터를 기존 인터페이스로 변환
 */
export const transformOptimizedOrderbookData = (optimizedData: OptimizedOrderbookData): RealtimeOrderbookMessage => {
  // 매도호가 (asks) - 높은 가격부터 내림차순
  const asks: OrderBookItem[] = [
    { price: parseFloat(optimizedData.askp10), quantity: parseFloat(optimizedData.askpRsqn10), size: 0 },
    { price: parseFloat(optimizedData.askp9), quantity: parseFloat(optimizedData.askpRsqn9), size: 0 },
    { price: parseFloat(optimizedData.askp8), quantity: parseFloat(optimizedData.askpRsqn8), size: 0 },
    { price: parseFloat(optimizedData.askp7), quantity: parseFloat(optimizedData.askpRsqn7), size: 0 },
    { price: parseFloat(optimizedData.askp6), quantity: parseFloat(optimizedData.askpRsqn6), size: 0 },
    { price: parseFloat(optimizedData.askp5), quantity: parseFloat(optimizedData.askpRsqn5), size: 0 },
    { price: parseFloat(optimizedData.askp4), quantity: parseFloat(optimizedData.askpRsqn4), size: 0 },
    { price: parseFloat(optimizedData.askp3), quantity: parseFloat(optimizedData.askpRsqn3), size: 0 },
    { price: parseFloat(optimizedData.askp2), quantity: parseFloat(optimizedData.askpRsqn2), size: 0 },
    { price: parseFloat(optimizedData.askp1), quantity: parseFloat(optimizedData.askpRsqn1), size: 0 },
  ].filter(item => item.price > 0); // 0인 호가 제거

  // 매수호가 (bids) - 높은 가격부터 내림차순
  const bids: OrderBookItem[] = [
    { price: parseFloat(optimizedData.bidp1), quantity: parseFloat(optimizedData.bidpRsqn1), size: 0 },
    { price: parseFloat(optimizedData.bidp2), quantity: parseFloat(optimizedData.bidpRsqn2), size: 0 },
    { price: parseFloat(optimizedData.bidp3), quantity: parseFloat(optimizedData.bidpRsqn3), size: 0 },
    { price: parseFloat(optimizedData.bidp4), quantity: parseFloat(optimizedData.bidpRsqn4), size: 0 },
    { price: parseFloat(optimizedData.bidp5), quantity: parseFloat(optimizedData.bidpRsqn5), size: 0 },
    { price: parseFloat(optimizedData.bidp6), quantity: parseFloat(optimizedData.bidpRsqn6), size: 0 },
    { price: parseFloat(optimizedData.bidp7), quantity: parseFloat(optimizedData.bidpRsqn7), size: 0 },
    { price: parseFloat(optimizedData.bidp8), quantity: parseFloat(optimizedData.bidpRsqn8), size: 0 },
    { price: parseFloat(optimizedData.bidp9), quantity: parseFloat(optimizedData.bidpRsqn9), size: 0 },
    { price: parseFloat(optimizedData.bidp10), quantity: parseFloat(optimizedData.bidpRsqn10), size: 0 },
  ].filter(item => item.price > 0); // 0인 호가 제거

  return {
    stockCode: optimizedData.stockCode,
    asks,
    bids,
    timestamp: optimizedData.timestamp.toString(),
  };
};