import type { RealtimePriceMessage, RealtimeOrderbookMessage } from '../api/types';
import type { StockInfo } from '../../../entities/stock/type';

/**
 * 개발용 목업 데이터
 * 서버 연결 없이도 UI 확인 가능
 */

// 목업 주식 정보
export const createMockStockInfo = (stockCode: string): StockInfo => ({
  id: 1,
  stockCode,
  stockName: getMockStockName(stockCode),
  stockType: 'STOCK',
  isActive: true,
});

// 목업 실시간 가격 데이터
export const createMockPriceData = (stockCode: string): RealtimePriceMessage => ({
  stockCode,
  currentPrice: getMockBasePrice(stockCode),
  change: 1200,
  changePercent: 1.71,
  volume: 1234567,
  timestamp: new Date().toISOString(),
});

// 목업 호가 데이터
export const createMockOrderbook = (stockCode: string): RealtimeOrderbookMessage => {
  const basePrice = getMockBasePrice(stockCode);

  return {
    stockCode,
    asks: [
      { price: basePrice + 800, quantity: 119417, size: 1.34 },
      { price: basePrice + 400, quantity: 329778, size: 3.68 },
      { price: basePrice + 200, quantity: 244413, size: 2.73 },
      { price: basePrice, quantity: 181658, size: 2.03 },
      { price: basePrice - 200, quantity: 187845, size: 2.1 },
    ],
    bids: [
      { price: basePrice - 400, quantity: 114635, size: 1.28 },
      { price: basePrice - 600, quantity: 19452, size: 0.22 },
      { price: basePrice - 800, quantity: 329778, size: 3.68 },
      { price: basePrice - 1000, quantity: 244413, size: 2.73 },
      { price: basePrice - 1200, quantity: 181658, size: 2.03 },
    ],
    timestamp: new Date().toISOString(),
  };
};


// 종목 코드별 주식명 매핑
const getMockStockName = (stockCode: string): string => {
  const stockNames: Record<string, string> = {
    '005930': '삼성전자',
    '000660': 'SK하이닉스',
    '035420': 'NAVER',
    '051910': 'LG화학',
    '006400': '삼성SDI',
    '028260': '삼성물산',
    '012330': '현대모비스',
    '207940': '삼성바이오로직스',
    '035720': '카카오',
    '068270': '셀트리온',
  };

  return stockNames[stockCode] || `주식명-${stockCode}`;
};

// 종목 코드별 기준 가격
const getMockBasePrice = (stockCode: string): number => {
  const basePrices: Record<string, number> = {
    '005930': 71400, // 삼성전자
    '000660': 125000, // SK하이닉스
    '035420': 180000, // NAVER
    '051910': 420000, // LG화학
    '006400': 250000, // 삼성SDI
    '028260': 45000, // 삼성물산
    '012330': 250000, // 현대모비스
    '207940': 850000, // 삼성바이오로직스
    '035720': 58200, // 카카오
    '068270': 178500, // 셀트리온
  };

  return basePrices[stockCode] || 50000;
};

// 개발 모드 체크 함수
export const isDevMode = (): boolean => {
  return import.meta.env.VITE_DEV_MODE === 'true';
};

// 실시간 가격 시뮬레이션 (개발 모드용)
export const createPriceSimulation = (
  stockCode: string,
  callback: (data: RealtimePriceMessage) => void
): (() => void) => {
  let basePrice = getMockBasePrice(stockCode);

  const interval = setInterval(() => {
    // 가격 변동 시뮬레이션 (-1% ~ +1%)
    const changePercent = (Math.random() - 0.5) * 0.02;
    basePrice = Math.max(basePrice * (1 + changePercent), basePrice * 0.95);

    const mockPrice: RealtimePriceMessage = {
      stockCode,
      currentPrice: Math.round(basePrice),
      change: Math.round(basePrice - getMockBasePrice(stockCode)),
      changePercent: Number(
        (((basePrice - getMockBasePrice(stockCode)) / getMockBasePrice(stockCode)) * 100).toFixed(2)
      ),
      volume: Math.floor(Math.random() * 1000000) + 500000,
      timestamp: new Date().toISOString(),
    };

    callback(mockPrice);
  }, 1000); // 1초마다 업데이트

  // 정리 함수 반환
  return () => clearInterval(interval);
};
