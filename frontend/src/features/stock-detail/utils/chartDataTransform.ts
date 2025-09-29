import type { StockChartData, StockChartItem, CandleData, VolumeData } from '../api/types';
import { dateStringToTimestamp } from './dateUtils';

/**
 * API 응답 데이터를 TradingView 캔들 차트 형식으로 변환
 * 한국 주식시장 관례에 맞게 전일 종가 기준으로 색상이 결정되도록 open/close 값 조정
 */
export const transformToCandleData = (chartData: StockChartItem[]): CandleData[] => {
  return chartData.map((item, index) => {
    const actualOpen = Number(item.openPrice);
    const actualHigh = Number(item.highPrice);
    const actualLow = Number(item.lowPrice);
    const actualClose = Number(item.closePrice);

    // 전일 종가 계산
    let previousClose = actualOpen; // 기본값: 당일 시가

    if (index > 0) {
      // 이전 날짜의 종가 사용
      previousClose = Number(chartData[index - 1].closePrice);
    }

    // 한국 관례에 맞게 TradingView 캔들 데이터 조정
    let adjustedOpen: number;
    let adjustedClose: number;

    if (actualClose >= previousClose) {
      // 상승: TradingView가 빨간색으로 표시하도록 close > open 설정
      adjustedOpen = Math.min(previousClose, actualClose);
      adjustedClose = Math.max(previousClose, actualClose);
    } else {
      // 하락: TradingView가 파란색으로 표시하도록 open > close 설정
      adjustedOpen = Math.max(previousClose, actualClose);
      adjustedClose = Math.min(previousClose, actualClose);
    }

    return {
      time: dateStringToTimestamp(item.tradingDate),
      open: adjustedOpen,
      high: actualHigh,
      low: actualLow,
      close: adjustedClose,
    };
  });
};

/**
 * API 응답 데이터를 TradingView 거래량 차트 형식으로 변환
 */
export const transformToVolumeData = (chartData: StockChartItem[]): VolumeData[] => {
  return chartData.map((item, index) => {
    const close = Number(item.closePrice);

    // 전일 종가 기준으로 색상 결정
    let previousClose = Number(item.openPrice); // 기본값: 당일 시가

    if (index > 0) {
      // 이전 날짜의 종가 사용
      previousClose = Number(chartData[index - 1].closePrice);
    }

    const color = close >= previousClose ? '#dc2626' : '#387eefff'; // 전일 종가 대비 상승: 빨강, 하락: 파랑

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
  const sortedChartData =
    apiData.chartData && apiData.chartData.length > 0
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
