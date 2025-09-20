import type { StockChartData, StockChartItem, CandleData, VolumeData } from '../api/types';
import { dateStringToTimestamp } from './dateUtils';

/**
 * API 응답 데이터를 TradingView 캔들 차트 형식으로 변환
 */
export const transformToCandleData = (chartData: StockChartItem[]): CandleData[] => {
  return chartData.map((item) => ({
    time: dateStringToTimestamp(item.tradingDate),
    open: Number(item.openPrice),
    high: Number(item.highPrice),
    low: Number(item.lowPrice),
    close: Number(item.closePrice),
  }));
};

/**
 * API 응답 데이터를 TradingView 거래량 차트 형식으로 변환
 */
export const transformToVolumeData = (chartData: StockChartItem[]): VolumeData[] => {
  return chartData.map((item) => {
    const open = Number(item.openPrice);
    const close = Number(item.closePrice);
    const color = close >= open ? '#dc2626' : '#2563eb'; // 상승: 빨강, 하락: 파랑

    return {
      time: dateStringToTimestamp(item.tradingDate),
      value: Number(item.volume),
      color,
    };
  });
};

/**
 * API 응답 데이터를 차트에 사용할 수 있는 형태로 변환
 */
export const transformChartData = (apiData: StockChartData) => {
  // 무한 스크롤에서는 이미 mergeInfiniteChartData에서 정렬됨
  // 단일 요청의 경우에만 reverse 적용
  const sortedChartData = apiData.chartData && apiData.chartData.length > 0
    ? [...apiData.chartData].sort((a, b) => a.tradingDate.localeCompare(b.tradingDate))
    : [];

  const candleData = transformToCandleData(sortedChartData);
  const volumeData = transformToVolumeData(sortedChartData);

  return {
    candleData,
    volumeData,
    stockInfo: {
      code: apiData.stockCode,
      name: apiData.stockName,
    },
    summary: apiData.summary,
  };
};