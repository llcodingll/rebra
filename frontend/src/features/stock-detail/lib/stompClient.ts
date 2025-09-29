import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import type {
  OptimizedPriceData,
  OptimizedOrderbookData,
  BulkSubscriptionRequest,
  BulkSubscriptionResponse,
  BulkUnsubscriptionRequest,
  WebSocketMessage,
} from '../api/types';
import { SUBSCRIPTION_DATA_TYPES, WS_MESSAGE_TYPES } from '../api/types';

interface StompClientCallbacks {
  onBulkSubscriptionResult?: (response: BulkSubscriptionResponse) => void;
  onPriceData?: (stockCode: string, data: OptimizedPriceData) => void;
  onOrderbookData?: (stockCode: string, data: OptimizedOrderbookData) => void;
  onError?: (error: string) => void;
  onConnect?: () => void;
  onDisconnect?: () => void;
}

export class StockStompClient {
  private client: Client;
  private isConnected = false;
  private subscriptions: Map<string, StompSubscription> = new Map();
  private callbacks: StompClientCallbacks = {};
  private subscribedStocks: Set<string> = new Set();

  constructor(callbacks: StompClientCallbacks = {}) {
    this.callbacks = callbacks;
    const wsEndpoint = import.meta.env.VITE_API_BASE_URL + '/ws';

    this.client = new Client({
      brokerURL: wsEndpoint,
      // debug: (str) => {
      //   console.log('STOMP Debug:', str);
      // },
      reconnectDelay: 0,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        this.isConnected = true;
        this.setupBulkSubscriptionListener();
        this.callbacks.onConnect?.();
      },

      onStompError: (frame) => {
        this.isConnected = false;
        const error = `STOMP 오류: ${frame.headers['message'] || '알 수 없는 오류'}`;
        console.error('❌', error);
        this.callbacks.onError?.(error);
      },

      onWebSocketError: (error) => {
        this.isConnected = false;
        console.error('❌ WebSocket 오류:', error);
        this.callbacks.onError?.('WebSocket 연결 오류');
      },

      onDisconnect: () => {
        this.isConnected = false;
        this.callbacks.onDisconnect?.();
      },
    });
  }

  /**
   * STOMP 서버에 연결
   */
  async connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      this.client.onConnect = () => {
        this.isConnected = true;
        this.setupBulkSubscriptionListener();
        this.callbacks.onConnect?.();
        resolve();
      };

      this.client.onStompError = (frame) => {
        this.isConnected = false;
        const error = `STOMP 연결 실패: ${frame.headers['message'] || '알 수 없는 오류'}`;
        console.error('❌', error);
        this.callbacks.onError?.(error);
        reject(new Error(error));
      };

      this.client.onWebSocketError = (error) => {
        console.error('❌ WebSocket 연결 오류:', error);
        this.callbacks.onError?.('WebSocket 연결 오류');
        reject(new Error('WebSocket 연결 오류'));
      };

      this.client.activate();
    });
  }

  /**
   * 일괄 구독 결과 수신 리스너 설정
   */
  private setupBulkSubscriptionListener(): void {
    const bulkResultChannel = '/user/queue/bulk/subscription';

    const subscription = this.client.subscribe(bulkResultChannel, (message: IMessage) => {
      try {
        const response: WebSocketMessage<BulkSubscriptionResponse> = JSON.parse(message.body);

        if (response.type === WS_MESSAGE_TYPES.BULK_SUBSCRIPTION_RESPONSE) {
          this.callbacks.onBulkSubscriptionResult?.(response.data);
        }
      } catch (error) {
        console.error('❌ 일괄 구독 결과 파싱 오류:', error, '원본:', message.body);
        this.callbacks.onError?.('일괄 구독 결과 파싱 오류');
      }
    });

    this.subscriptions.set('bulk-subscription-result', subscription);
  }

  /**
   * 여러 종목을 일괄 구독 (기본적으로 호가+체결가 모두 구독)
   */
  async subscribeBulkStocks(stockCodes: string[]): Promise<void> {
    if (!this.isConnected) {
      throw new Error('STOMP가 연결되지 않음');
    }

    // 실시간 데이터 수신 채널 먼저 구독
    this.setupRealtimeDataChannels(stockCodes);

    // 일괄 구독 요청 생성
    const request: BulkSubscriptionRequest = {
      stocks: stockCodes.map((stockCode) => ({
        stockCode,
        dataTypes: [SUBSCRIPTION_DATA_TYPES.PRICE, SUBSCRIPTION_DATA_TYPES.ORDERBOOK],
      })),
    };

    // 서버로 일괄 구독 요청 전송
    this.client.publish({
      destination: '/app/subscribe/bulk',
      body: JSON.stringify(request),
    });

    // 구독된 종목 목록 업데이트
    stockCodes.forEach((code) => this.subscribedStocks.add(code));
  }

  /**
   * 실시간 데이터 수신 채널 설정
   */
  private setupRealtimeDataChannels(stockCodes: string[]): void {
    stockCodes.forEach((stockCode) => {
      // 체결가 데이터 채널
      const priceChannel = `/user/queue/stock/${stockCode}/price`;
      if (!this.subscriptions.has(`price-${stockCode}`)) {
        const priceSubscription = this.client.subscribe(priceChannel, (message: IMessage) => {
          try {
            const optimizedData: OptimizedPriceData = JSON.parse(message.body);
            this.callbacks.onPriceData?.(stockCode, optimizedData);
          } catch (error) {
            console.error(`❌ 체결가 데이터 파싱 오류 [${stockCode}]:`, error);
          }
        });
        this.subscriptions.set(`price-${stockCode}`, priceSubscription);
      }

      // 호가 데이터 채널
      const orderbookChannel = `/user/queue/stock/${stockCode}/orderbook`;
      if (!this.subscriptions.has(`orderbook-${stockCode}`)) {
        const orderbookSubscription = this.client.subscribe(orderbookChannel, (message: IMessage) => {
          try {
            const optimizedData: OptimizedOrderbookData = JSON.parse(message.body);
            this.callbacks.onOrderbookData?.(stockCode, optimizedData);
          } catch (error) {
            console.error(`❌ 호가 데이터 파싱 오류 [${stockCode}]:`, error);
          }
        });
        this.subscriptions.set(`orderbook-${stockCode}`, orderbookSubscription);
      }
    });
  }

  /**
   * 일괄 구독 해제
   */
  async unsubscribeBulkStocks(stockCodes: string[]): Promise<void> {
    if (!this.isConnected) {
      throw new Error('STOMP가 연결되지 않음');
    }

    const request: BulkUnsubscriptionRequest = {
      stocks: stockCodes.map((stockCode) => ({
        stockCode,
        dataTypes: ['all'], // 모든 데이터 타입 해제
      })),
    };

    // 서버로 일괄 구독 해제 요청 전송
    this.client.publish({
      destination: '/app/unsubscribe/bulk',
      body: JSON.stringify(request),
    });

    // 클라이언트 측 구독 해제
    stockCodes.forEach((stockCode) => {
      this.unsubscribeRealtimeChannels(stockCode);
      this.subscribedStocks.delete(stockCode);
    });
  }

  /**
   * 특정 종목의 실시간 데이터 채널 구독 해제
   */
  private unsubscribeRealtimeChannels(stockCode: string): void {
    // 체결가 채널 해제
    const priceKey = `price-${stockCode}`;
    const priceSubscription = this.subscriptions.get(priceKey);
    if (priceSubscription) {
      priceSubscription.unsubscribe();
      this.subscriptions.delete(priceKey);
    }

    // 호가 채널 해제
    const orderbookKey = `orderbook-${stockCode}`;
    const orderbookSubscription = this.subscriptions.get(orderbookKey);
    if (orderbookSubscription) {
      orderbookSubscription.unsubscribe();
      this.subscriptions.delete(orderbookKey);
    }
  }

  /**
   * 모든 구독 해제 및 연결 종료
   */
  disconnect(): void {
    // 모든 구독 해제
    this.subscriptions.forEach((subscription, key) => {
      try {
        subscription.unsubscribe();
      } catch (error) {
        console.error(`❌ 구독 해제 오류 [${key}]:`, error);
      }
    });

    this.subscriptions.clear();
    this.subscribedStocks.clear();

    // STOMP 클라이언트 연결 해제
    if (this.client.connected) {
      this.client.deactivate();
    }

    // WebSocket 강제 종료 (추가 안전장치)
    try {
      if (this.client.webSocket && this.client.webSocket.readyState !== WebSocket.CLOSED) {
        this.client.webSocket.close();
      }
    } catch (error) {
      console.error('❌ WebSocket 강제 종료 실패:', error);
    }

    this.isConnected = false;
  }

  /**
   * 연결 상태 확인
   */
  getConnectionStatus(): boolean {
    return this.isConnected && this.client.connected;
  }

  /**
   * WebSocket 연결 상태 상세 확인
   */
  getDetailedConnectionStatus(): {
    isConnected: boolean;
    clientConnected: boolean;
    webSocketState: number | null;
    webSocketStateText: string;
  } {
    const webSocketState = this.client.webSocket?.readyState || null;
    const webSocketStateText =
      webSocketState !== null
        ? ['CONNECTING', 'OPEN', 'CLOSING', 'CLOSED'][webSocketState] || 'UNKNOWN'
        : 'NO_WEBSOCKET';

    return {
      isConnected: this.isConnected,
      clientConnected: this.client.connected,
      webSocketState,
      webSocketStateText,
    };
  }

  /**
   * 연결이 완전히 정리되었는지 확인
   */
  isFullyDisconnected(): boolean {
    const status = this.getDetailedConnectionStatus();
    return (
      !status.isConnected &&
      !status.clientConnected &&
      (status.webSocketState === WebSocket.CLOSED || status.webSocketState === null)
    );
  }

  /**
   * 현재 구독된 종목 목록 반환
   */
  getSubscribedStocks(): string[] {
    return Array.from(this.subscribedStocks);
  }

  /**
   * 콜백 함수 업데이트
   */
  updateCallbacks(callbacks: Partial<StompClientCallbacks>): void {
    this.callbacks = { ...this.callbacks, ...callbacks };
  }
}
