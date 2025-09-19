import { useState, useEffect, useRef } from 'react';
import { StockStompClient } from '../lib/stompClient';
import {
  isDevMode,
  createMockStockInfo,
  createMockPriceData,
  createMockOrderbook,
  createPriceSimulation,
} from '../lib/mockData';
import type { StockInfo, RealtimePriceMessage, RealtimeOrderbookMessage } from '../api/types';

// 전역 연결 카운터
let globalConnectionCounter = 0;

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

  // 상세 상태 (테스트/디버깅용)
  connectionDetails: {
    stompConnected: boolean;
    priceSubscribed: boolean;
    orderbookSubscribed: boolean;
    lastPriceUpdate: string | null;
    lastOrderbookUpdate: string | null;
    connectionAttempts: number;
  };

  // 수동 제어 함수
  disconnect: () => void;
  reconnect: () => void;
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

  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 상세 상태 관리 (테스트/디버깅용)
  const [connectionDetails, setConnectionDetails] = useState({
    stompConnected: false,
    priceSubscribed: false,
    orderbookSubscribed: false,
    lastPriceUpdate: null as string | null,
    lastOrderbookUpdate: null as string | null,
    connectionAttempts: 0,
  });

  // STOMP 클라이언트 참조
  const stompClientRef = useRef<StockStompClient | null>(null);
  const simulationCleanupRef = useRef<(() => void) | null>(null);

  // 중복 연결 방지를 위한 상태 관리
  const isConnectingRef = useRef<boolean>(false);
  const hookIdRef = useRef<string>(`hook-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`);

  useEffect(() => {
    const currentHookId = hookIdRef.current;
    globalConnectionCounter++;

    console.log('🚀 [CONNECTION-TRACKER] =================================');
    console.log('🚀 [CONNECTION-TRACKER] useEffect 실행 시작');
    console.log('🚀 [CONNECTION-TRACKER] Hook ID:', currentHookId);
    console.log('🚀 [CONNECTION-TRACKER] 전역 연결 카운터:', globalConnectionCounter);
    console.log('🚀 [CONNECTION-TRACKER] stockCode:', stockCode);
    console.log('🚀 [CONNECTION-TRACKER] 현재 연결 중 상태:', isConnectingRef.current);
    console.log('🚀 [CONNECTION-TRACKER] 기존 STOMP 클라이언트 존재:', !!stompClientRef.current);
    console.log('🚀 [CONNECTION-TRACKER] =================================');

    if (!stockCode) {
      console.warn('⚠️ [' + currentHookId + '] stockCode가 제공되지 않음');
      return;
    }

    // 중복 연결 방지 체크
    if (isConnectingRef.current || stompClientRef.current?.getConnectionStatus()) {
      console.log('🛑 [' + currentHookId + '] 이미 연결 중이거나 연결됨. 중복 연결 방지.');
      console.log('🛑 [' + currentHookId + '] isConnecting:', isConnectingRef.current);
      console.log('🛑 [' + currentHookId + '] 기존 연결 상태:', stompClientRef.current?.getConnectionStatus());
      return;
    }

    let mounted = true;

    // 🛠️ 개발 모드: 서버 연결 없이 목업 데이터 사용
    if (true) {
      console.log('🛠️ 개발 모드: 목업 데이터로 실행', stockCode);

      setIsLoading(true);

      // 목업 데이터 설정 (약간의 지연으로 실제와 유사하게)
      setTimeout(() => {
        if (!mounted) return;

        const mockStock = createMockStockInfo(stockCode);
        const mockPrice = createMockPriceData(stockCode);
        const mockOrderbook = createMockOrderbook(stockCode);

        setStockInfo(mockStock);
        setRealtimePrice(mockPrice);
        setOrderbook(mockOrderbook);
        setIsConnected(false); // 개발 모드는 실제 연결 아님
        setIsLoading(false);
        // 실시간 가격 시뮬레이션 시작
        const cleanup = createPriceSimulation(stockCode, (priceData) => {
          if (!mounted) return;
          setRealtimePrice(priceData);
        });

        simulationCleanupRef.current = cleanup;
      }, 500);

      return;
    }

    const initializeRealtimeConnection = async () => {
      try {
        isConnectingRef.current = true;
        console.log('🔄 [' + currentHookId + '] 연결 프로세스 시작. isConnecting = true');

        setIsLoading(true);
        setError(null);
        setConnectionDetails((prev) => ({
          ...prev,
          connectionAttempts: prev.connectionAttempts + 1,
        }));

        console.log('🚀 [' + currentHookId + '] 실시간 주식 데이터 초기화 시작:', stockCode);
        console.log('🚀 [' + currentHookId + '] 개발 모드:', isDevMode());

        // 기본 주식 정보 설정 (하드코딩)
        const basicStockInfo: StockInfo = {
          id: 1,
          stockCode: stockCode,
          stockName: '테스트 주식', // 실제로는 별도 API나 정적 데이터에서 가져와야 함
          stockType: 'STOCK',
          isActive: true,
        };
        setStockInfo(basicStockInfo);

        // 하드코딩된 WebSocket 엔드포인트
        // const wsEndpoint = 'http://localhost:8080/ws';
        // console.log('🔌 [Hook] WebSocket 엔드포인트:', wsEndpoint);
        // console.log('🔌 [Hook] 생성할 구독 채널:', [
        //   `/user/queue/stock/${stockCode}/price`,
        //   `/user/queue/stock/${stockCode}/orderbook`,
        // ]);

        // STOMP 클라이언트 연결
        const stompClient = new StockStompClient();
        stompClientRef.current = stompClient;

        try {
          await stompClient.connect();

          if (!mounted) {
            console.log('⚠️ [' + currentHookId + '] 연결 완료 후 컴포넌트 unmount됨');
            return;
          }

          setIsConnected(true);
          setConnectionDetails((prev) => ({
            ...prev,
            stompConnected: true,
          }));
          console.log('✅ [' + currentHookId + '] STOMP 연결 성공');
          isConnectingRef.current = false;
          console.log('🔄 [' + currentHookId + '] 연결 완료. isConnecting = false');

          // 실시간 가격 정보 구독 (Queue 기반)
          console.log('📊 [Hook] 가격 정보 구독 시작...');
          stompClient.subscribePriceData(stockCode, (priceData: RealtimePriceMessage) => {
            console.log('🎪 [HOOK-PRICE] =================================');
            console.log('🎪 [HOOK-PRICE] 가격 콜백 함수 실행 시작!');
            console.log('🎪 [HOOK-PRICE] 실행 시간:', new Date().toISOString());
            console.log('🎪 [HOOK-PRICE] mounted 상태:', mounted);
            console.log('🎪 [HOOK-PRICE] 받은 가격 데이터:', priceData);

            if (!mounted) {
              console.log('⚠️ [HOOK-PRICE] 컴포넌트가 unmount됨. 상태 업데이트 생략.');
              console.log('🎪 [HOOK-PRICE] =================================');
              return;
            }

            console.log('🎪 [HOOK-PRICE] React 상태 업데이트 시도 중...');
            setRealtimePrice(priceData);
            console.log('✅ [HOOK-PRICE] setRealtimePrice 호출 완료');

            setConnectionDetails((prev) => {
              const newDetails = {
                ...prev,
                priceSubscribed: true,
                lastPriceUpdate: new Date().toISOString(),
              };
              console.log('🎪 [HOOK-PRICE] connectionDetails 업데이트:', newDetails);
              return newDetails;
            });
            console.log('✅ [HOOK-PRICE] setConnectionDetails 호출 완료');
            console.log('🎪 [HOOK-PRICE] 가격 콜백 함수 실행 완료!');
            console.log('🎪 [HOOK-PRICE] =================================');
          });

          // 실시간 호가 정보 구독 (Queue 기반)
          stompClient.subscribeOrderbook(stockCode, (orderbookData: RealtimeOrderbookMessage) => {
            console.log('🎭 [HOOK-ORDERBOOK] =================================');
            console.log('🎭 [HOOK-ORDERBOOK] 호가 콜백 함수 실행 시작!');
            console.log('🎭 [HOOK-ORDERBOOK] 실행 시간:', new Date().toISOString());
            console.log('🎭 [HOOK-ORDERBOOK] mounted 상태:', mounted);
            console.log('🎭 [HOOK-ORDERBOOK] 받은 호가 데이터:', orderbookData);

            if (!mounted) {
              console.log('⚠️ [HOOK-ORDERBOOK] 컴포넌트가 unmount됨. 상태 업데이트 생략.');
              console.log('🎭 [HOOK-ORDERBOOK] =================================');
              return;
            }

            console.log('🎭 [HOOK-ORDERBOOK] React 상태 업데이트 시도 중...');
            setOrderbook(orderbookData);
            console.log('✅ [HOOK-ORDERBOOK] setOrderbook 호출 완료');

            setConnectionDetails((prev) => {
              const newDetails = {
                ...prev,
                orderbookSubscribed: true,
                lastOrderbookUpdate: new Date().toISOString(),
              };
              console.log('🎭 [HOOK-ORDERBOOK] connectionDetails 업데이트:', newDetails);
              return newDetails;
            });
            console.log('✅ [HOOK-ORDERBOOK] setConnectionDetails 호출 완료');
            console.log('🎭 [HOOK-ORDERBOOK] 호가 콜백 함수 실행 완료!');
            console.log('🎭 [HOOK-ORDERBOOK] =================================');
          });

          setConnectionDetails((prev) => ({
            ...prev,
            priceSubscribed: true,
            orderbookSubscribed: true,
          }));
        } catch (stompError) {
          if (!mounted) return;

          console.error('❌ [' + currentHookId + '] STOMP 연결 실패:', stompError);
          setError('실시간 데이터 연결에 실패했습니다');
          setIsConnected(false);
          isConnectingRef.current = false;
          console.log('🔄 [' + currentHookId + '] 연결 실패. isConnecting = false');
          setConnectionDetails((prev) => ({
            ...prev,
            stompConnected: false,
            priceSubscribed: false,
            orderbookSubscribed: false,
          }));
        }
      } catch (error) {
        if (!mounted) return;

        console.error('❌ [' + currentHookId + '] 실시간 연결 초기화 실패:', error);
        setError('주식 정보 로딩 중 오류가 발생했습니다');
        isConnectingRef.current = false;
        console.log('🔄 [' + currentHookId + '] 초기화 실패. isConnecting = false');
        setConnectionDetails((prev) => ({
          ...prev,
          stompConnected: false,
          priceSubscribed: false,
          orderbookSubscribed: false,
        }));
      } finally {
        if (mounted) {
          setIsLoading(false);
        }
      }
    };

    initializeRealtimeConnection();

    // 클린업 함수
    return () => {
      globalConnectionCounter--;
      mounted = false;

      console.log('🧹 [CLEANUP] =================================');
      console.log('🧹 [CLEANUP] Hook ID:', currentHookId);
      console.log('🧹 [CLEANUP] cleanup 함수 실행 시작');
      console.log('🧹 [CLEANUP] 전역 연결 카운터 (cleanup 후):', globalConnectionCounter);
      console.log('🧹 [CLEANUP] isConnecting 상태:', isConnectingRef.current);
      console.log('🧹 [CLEANUP] STOMP 클라이언트 존재:', !!stompClientRef.current);

      // 개발 모드 시뮬레이션 정리
      if (simulationCleanupRef.current) {
        console.log('🧹 [' + currentHookId + '] 개발 모드 시뮬레이션 정리 중...');
        simulationCleanupRef.current();
        simulationCleanupRef.current = null;
      }

      // STOMP 연결 정리
      if (stompClientRef.current) {
        console.log('🧹 [' + currentHookId + '] 실시간 연결 정리 중...');
        stompClientRef.current.disconnect();
        stompClientRef.current = null;
      }

      // 연결 상태 초기화
      isConnectingRef.current = false;
      console.log('🔄 [' + currentHookId + '] cleanup 완료. isConnecting = false');

      setIsConnected(false);
      setIsLoading(false);
      setConnectionDetails({
        stompConnected: false,
        priceSubscribed: false,
        orderbookSubscribed: false,
        lastPriceUpdate: null,
        lastOrderbookUpdate: null,
        connectionAttempts: 0,
      });

      console.log('🧹 [CLEANUP] cleanup 함수 실행 완료');
      console.log('🧹 [CLEANUP] =================================');
    };
  }, [stockCode]);

  // 수동 제어 함수
  const disconnect = () => {
    if (stompClientRef.current) {
      console.log('🔌 수동 연결 해제');
      stompClientRef.current.disconnect();
      stompClientRef.current = null;
      setIsConnected(false);
      setConnectionDetails((prev) => ({
        ...prev,
        stompConnected: false,
        priceSubscribed: false,
        orderbookSubscribed: false,
      }));
    }
  };

  const reconnect = () => {
    // 재연결 기능 비활성화
    console.log('⚠️ 재연결 기능이 비활성화되었습니다. 페이지를 새로고침해주세요.');
  };

  return {
    stockInfo,
    realtimePrice,
    orderbook,
    isConnected,
    isLoading,
    error,
    connectionDetails,
    disconnect,
    reconnect,
  };
}
