import { useState, useEffect, useRef } from 'react';
import { stockApi } from '../api/stockApi';
import { StockStompClient } from '../lib/stompClient';
import type {
  StockInfo,
  RealtimeData,
  RealtimePriceMessage,
  RealtimeOrderbookMessage,
  WebSocketInfo
} from '../api/types';

interface UseRealtimeStockReturn {
  // 기본 주식 정보
  stockInfo: StockInfo | null;

  // 실시간 데이터
  realtimePrice: RealtimePriceMessage | null;
  orderbook: RealtimeOrderbookMessage | null;

  // 연결 상태
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;

  // WebSocket 정보
  webSocketInfo: WebSocketInfo | null;
}

/**
 * 실시간 주식 데이터를 관리하는 커스텀 훅
 * @param stockCode 주식 코드 (예: "005930")
 * @returns 주식 정보, 실시간 데이터, 연결 상태
 */
export function useRealtimeStock(stockCode: string): UseRealtimeStockReturn {
  // 상태 관리
  const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
  const [realtimePrice, setRealtimePrice] = useState<RealtimePriceMessage | null>(null);
  const [orderbook, setOrderbook] = useState<RealtimeOrderbookMessage | null>(null);
  const [webSocketInfo, setWebSocketInfo] = useState<WebSocketInfo | null>(null);

  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // STOMP 클라이언트 참조
  const stompClientRef = useRef<StockStompClient | null>(null);

  useEffect(() => {
    if (!stockCode) {
      console.warn('⚠️ stockCode가 제공되지 않음');
      return;
    }

    let mounted = true;

    const initializeRealtimeConnection = async () => {
      try {
        setIsLoading(true);
        setError(null);

        console.log('🚀 실시간 주식 데이터 초기화 시작:', stockCode);

        // 1. API 호출하여 초기 데이터 및 WebSocket 정보 가져오기
        const result = await stockApi.getStockDetail(stockCode);

        if (!mounted) return;

        if (result.success) {
          const responseData = result.data;
          const { stock, realtime, webSocketInfo: wsInfo } = responseData.data;

          console.log('📊 주식 기본 정보:', stock);
          console.log('⚡ 초기 실시간 데이터:', realtime);
          console.log('🔌 WebSocket 연결 정보:', wsInfo);

          // 상태 업데이트
          setStockInfo(stock);
          setWebSocketInfo(wsInfo);

          // 초기 실시간 데이터가 있다면 설정
          if (realtime.currentPrice?.priceData) {
            try {
              // priceData가 JSON 문자열이라면 파싱
              const initialPriceData = typeof realtime.currentPrice.priceData === 'string'
                ? JSON.parse(realtime.currentPrice.priceData)
                : realtime.currentPrice.priceData;

              setRealtimePrice({
                stockCode: realtime.currentPrice.stockCode,
                timestamp: realtime.currentPrice.timestamp,
                ...initialPriceData
              });
            } catch (parseError) {
              console.warn('⚠️ 초기 가격 데이터 파싱 실패:', parseError);
            }
          }

          if (realtime.orderbook?.orderbookData) {
            try {
              // orderbookData가 JSON 문자열이라면 파싱
              const initialOrderbookData = typeof realtime.orderbook.orderbookData === 'string'
                ? JSON.parse(realtime.orderbook.orderbookData)
                : realtime.orderbook.orderbookData;

              setOrderbook({
                stockCode: realtime.orderbook.stockCode,
                timestamp: realtime.orderbook.timestamp,
                ...initialOrderbookData
              });
            } catch (parseError) {
              console.warn('⚠️ 초기 호가 데이터 파싱 실패:', parseError);
            }
          }

          // 2. STOMP 클라이언트 연결
          const stompClient = new StockStompClient(wsInfo.endpoint);
          stompClientRef.current = stompClient;

          try {
            await stompClient.connect();

            if (!mounted) return;

            setIsConnected(true);
            console.log('✅ STOMP 연결 성공');

            // 3. 실시간 가격 정보 구독
            stompClient.subscribePriceData(
              wsInfo.priceChannel,
              (priceData: RealtimePriceMessage) => {
                if (!mounted) return;

                console.log('💰 실시간 가격 업데이트:', priceData);
                setRealtimePrice(priceData);
              }
            );

            // 4. 실시간 호가 정보 구독
            stompClient.subscribeOrderbook(
              wsInfo.orderbookChannel,
              (orderbookData: RealtimeOrderbookMessage) => {
                if (!mounted) return;

                console.log('📈 실시간 호가 업데이트:', orderbookData);
                setOrderbook(orderbookData);
              }
            );

          } catch (stompError) {
            if (!mounted) return;

            console.error('❌ STOMP 연결 실패:', stompError);
            setError('실시간 데이터 연결에 실패했습니다');
            setIsConnected(false);
          }

        } else {
          // API 에러 처리
          const errorMessage = result.error?.message || '주식 정보를 가져올 수 없습니다';
          console.error('❌ API 호출 실패:', errorMessage);
          setError(errorMessage);
        }

      } catch (error) {
        if (!mounted) return;

        console.error('❌ 실시간 연결 초기화 실패:', error);
        setError('주식 정보 로딩 중 오류가 발생했습니다');
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    };

    initializeRealtimeConnection();

    // 클린업 함수
    return () => {
      mounted = false;

      if (stompClientRef.current) {
        console.log('🧹 실시간 연결 정리 중...');
        stompClientRef.current.disconnect();
        stompClientRef.current = null;
      }

      setIsConnected(false);
      setIsLoading(false);
    };
  }, [stockCode]);

  return {
    stockInfo,
    realtimePrice,
    orderbook,
    webSocketInfo,
    isConnected,
    isLoading,
    error
  };
}