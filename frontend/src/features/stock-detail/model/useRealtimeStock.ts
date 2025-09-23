import { useState, useEffect, useRef, useCallback, useMemo } from 'react';
import { StockStompClient } from '../lib/stompClient';
import {
  isDevMode,
  createMockStockInfo,
  createMockPriceData,
  createMockOrderbook,
  createPriceSimulation,
} from '../lib/mockData';
import type { OptimizedPriceData, OptimizedOrderbookData, BulkSubscriptionResponse } from '../api/types';
import type { StockInfo } from '../../../entities/stock/type';

interface UseRealtimeStockReturn {
  // 기본 주식 정보
  stockInfo: StockInfo | null;

  // 실시간 데이터
  realtimePrice: OptimizedPriceData | null;
  orderbook: OptimizedOrderbookData | null;

  // 연결 상태
  isConnected: boolean;
  isLoading: boolean;
  error: string | null;

  // 구독 상태
  subscriptionStatus: {
    requested: boolean;
    successful: boolean;
    failed: boolean;
    lastUpdate: string | null;
  };

  // 수동 제어 함수
  disconnect: () => void;
  reconnect: () => void;
}

/**
 * 실시간 주식 데이터를 관리하는 커스텀 훅 (일괄 구독 방식)
 * @param stockCode 주식 코드 (예: "005930")
 * @returns 주식 정보, 실시간 데이터, 연결 상태
 */
export function useRealtimeStock(stockCode: string): UseRealtimeStockReturn {
  // 기본 상태
  const [stockInfo, setStockInfo] = useState<StockInfo | null>(null);
  const [realtimePrice, setRealtimePrice] = useState<OptimizedPriceData | null>(null);
  const [orderbook, setOrderbook] = useState<OptimizedOrderbookData | null>(null);

  // 연결 상태
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 구독 상태 (최적화된 객체)
  const [subscriptionStatus, setSubscriptionStatus] = useState(() => ({
    requested: false,
    successful: false,
    failed: false,
    lastUpdate: null as string | null,
  }));

  // Refs
  const stompClientRef = useRef<StockStompClient | null>(null);
  const simulationCleanupRef = useRef<(() => void) | null>(null);
  const isInitializingRef = useRef(false);
  const mountedRef = useRef(true);

  // 안정화된 콜백들 (무한 리렌더링 방지)
  const handleConnect = useCallback(() => {
    if (!mountedRef.current) return;
    setIsConnected(true);
    setError(null);
    console.log('✅ STOMP 연결 완료');
  }, []);

  const handleDisconnect = useCallback(() => {
    if (!mountedRef.current) return;
    setIsConnected(false);
    console.log('🔌 STOMP 연결 해제');
  }, []);

  const handleError = useCallback((errorMessage: string) => {
    if (!mountedRef.current) return;
    setError(errorMessage);
    setIsLoading(false);
    console.error('❌ STOMP 오류:', errorMessage);
  }, []);

  const handleBulkSubscriptionResult = useCallback((response: BulkSubscriptionResponse) => {
    if (!mountedRef.current) return;

    console.log('📥 일괄 구독 결과:', response);

    const currentTime = new Date().toISOString();
    const hasSuccess = response.summary.totalSuccessful > 0;
    const hasFailed = response.summary.totalFailed > 0;

    setSubscriptionStatus({
      requested: true,
      successful: hasSuccess,
      failed: hasFailed,
      lastUpdate: currentTime,
    });

    if (hasSuccess) {
      console.log(`✅ 구독 성공: ${response.summary.totalSuccessful}개`);
    }
    if (hasFailed) {
      console.warn(`⚠️ 구독 실패: ${response.summary.totalFailed}개`);
    }
  }, []);

  const handlePriceData = useCallback(
    (receivedStockCode: string, data: OptimizedPriceData) => {
      if (!mountedRef.current || receivedStockCode !== stockCode) return;

      setRealtimePrice(data);
    },
    [stockCode]
  );

  const handleOrderbookData = useCallback(
    (receivedStockCode: string, data: OptimizedOrderbookData) => {
      if (!mountedRef.current || receivedStockCode !== stockCode) return;

      setOrderbook(data);
    },
    [stockCode]
  );

  // STOMP 클라이언트 생성 (stockCode 변경 시에만)
  const stompClient = useMemo(() => {
    if (!stockCode) return null;

    return new StockStompClient({
      onConnect: handleConnect,
      onDisconnect: handleDisconnect,
      onError: handleError,
      onBulkSubscriptionResult: handleBulkSubscriptionResult,
      onPriceData: handlePriceData,
      onOrderbookData: handleOrderbookData,
    });
  }, [
    stockCode,
    handleConnect,
    handleDisconnect,
    handleError,
    handleBulkSubscriptionResult,
    handlePriceData,
    handleOrderbookData,
  ]);

  // 연결 및 구독 초기화
  const initializeConnection = useCallback(async () => {
    if (!stockCode || !stompClient || isInitializingRef.current) {
      return;
    }

    try {
      isInitializingRef.current = true;
      setIsLoading(true);
      setError(null);

      // 개발 모드에서는 목업 데이터 사용
      if (false) {
        // isDevMode()
        console.log('🛠️ 개발 모드: 목업 데이터 사용');

        setTimeout(() => {
          if (!mountedRef.current) return;

          const mockStock = createMockStockInfo(stockCode);
          const mockPrice = createMockPriceData(stockCode);
          const mockOrderbook = createMockOrderbook(stockCode);

          setStockInfo(mockStock);
          setRealtimePrice(mockPrice);
          setOrderbook(mockOrderbook);
          setIsConnected(false); // 개발 모드는 실제 연결 아님
          setIsLoading(false);

          // 시뮬레이션 시작
          const cleanup = createPriceSimulation(stockCode, (priceData) => {
            if (mountedRef.current) {
              setRealtimePrice(priceData);
            }
          });

          simulationCleanupRef.current = cleanup;
        }, 300);

        return;
      }

      // 실제 STOMP 연결
      console.log(`🚀 실시간 연결 시작: ${stockCode}`);

      await stompClient.connect();

      if (!mountedRef.current) return;

      // 일괄 구독 요청
      await stompClient.subscribeBulkStocks([stockCode]);

      setIsLoading(false);
      console.log(`✅ 구독 요청 완료: ${stockCode}`);
    } catch (error) {
      if (!mountedRef.current) return;

      const errorMessage = error instanceof Error ? error.message : '연결 실패';
      setError(errorMessage);
      setIsLoading(false);
      console.error('❌ 연결 실패:', error);
    } finally {
      isInitializingRef.current = false;
    }
  }, [stockCode, stompClient]);

  // 수동 재연결
  const reconnect = useCallback(() => {
    console.log('🔄 수동 재연결 시작');
    if (stompClient) {
      stompClient.disconnect();
    }
    initializeConnection();
  }, [stompClient, initializeConnection]);

  // 수동 연결 해제
  const disconnect = useCallback(() => {
    console.log('🔌 수동 연결 해제');

    // 시뮬레이션 정리
    if (simulationCleanupRef.current) {
      simulationCleanupRef.current();
      simulationCleanupRef.current = null;
    }

    // STOMP 연결 해제
    if (stompClient) {
      stompClient.disconnect();
    }

    // 상태 초기화
    setIsConnected(false);
    setIsLoading(false);
    setSubscriptionStatus({
      requested: false,
      successful: false,
      failed: false,
      lastUpdate: null,
    });
  }, [stompClient]);

  // stockCode 변경 시 연결 초기화
  useEffect(() => {
    if (!stockCode) {
      console.warn('⚠️ stockCode가 제공되지 않음');
      return;
    }

    // 이전 연결 정리
    if (stompClientRef.current) {
      stompClientRef.current.disconnect();
    }

    // 새 클라이언트 저장
    stompClientRef.current = stompClient;

    // 연결 시작
    initializeConnection();

    // 클린업 함수
    return () => {
      console.log(`🧹 useRealtimeStock cleanup: ${stockCode}`);

      // 시뮬레이션 정리
      if (simulationCleanupRef.current) {
        simulationCleanupRef.current();
        simulationCleanupRef.current = null;
      }

      // STOMP 연결 정리
      if (stompClientRef.current) {
        stompClientRef.current.disconnect();
        stompClientRef.current = null;
      }

      // 초기화 플래그 리셋
      isInitializingRef.current = false;
    };
  }, [stockCode, stompClient, initializeConnection]);

  // 컴포넌트 언마운트 시 정리
  useEffect(() => {
    mountedRef.current = true;

    return () => {
      mountedRef.current = false;
    };
  }, []);

  return {
    stockInfo,
    realtimePrice,
    orderbook,
    isConnected,
    isLoading,
    error,
    subscriptionStatus,
    disconnect,
    reconnect,
  };
}
