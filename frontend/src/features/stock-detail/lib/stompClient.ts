import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import type {
  OptimizedPriceData,
  OptimizedOrderbookData,
  RealtimePriceMessage,
  RealtimeOrderbookMessage,
} from '../api/types';
import { transformOptimizedPriceData, transformOptimizedOrderbookData } from '../utils/krxDataTransform';

export class StockStompClient {
  private client: Client;
  private isConnected = false;
  private subscriptions: Map<string, StompSubscription> = new Map();

  constructor() {
    const wsEndpoint = import.meta.env.VITE_API_BASE_URL + '/ws';

    this.client = new Client({
      brokerURL: wsEndpoint,
      debug: (str) => {},
      reconnectDelay: 0,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        this.isConnected = true;
      },

      onStompError: (frame) => {
        this.isConnected = false;
      },

      onWebSocketError: (error) => {
        this.isConnected = false;
      },

      onDisconnect: () => {
        this.isConnected = false;
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
        resolve();
      };

      this.client.onStompError = (frame) => {
        this.isConnected = false;
        reject(new Error(`STOMP 연결 실패: ${frame.headers['message'] || '알 수 없는 오류'}`));
      };

      this.client.onWebSocketError = (error) => {};

      this.client.activate();
    });
  }

  /**
   * 실시간 가격 정보 구독
   * @param userId 사용자 ID
   * @param stockCode 종목 코드
   * @param callback 가격 데이터 수신 콜백
   */
  subscribePriceData(stockCode: string, callback: (data: RealtimePriceMessage) => void): void {
    if (!this.isConnected) {
      console.warn('STOMP가 연결되지 않음. 가격 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/price`;

    // 1) 수신용 SUBSCRIBE
    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        const optimizedData: OptimizedPriceData = JSON.parse(message.body);
        const priceData = transformOptimizedPriceData(optimizedData);
        callback(priceData);
      } catch (error) {
        console.error('가격 데이터 파싱 에러:', error, '원본:', message.body);
      }
    });
    this.subscriptions.set(`price-${stockCode}`, subscription);

    // 2) 서버로 구독 요청 SEND (→ @MessageMapping)
    this.client.publish({
      destination: `/app/subscribe/${stockCode}/price`,
      body: '',
    });
  }

  /**
   * 실시간 호가 정보 구독
   * @param userId 사용자 ID
   * @param stockCode 종목 코드
   * @param callback 호가 데이터 수신 콜백
   */
  subscribeOrderbook(stockCode: string, callback: (data: RealtimeOrderbookMessage) => void): void {
    if (!this.isConnected) {
      console.warn('STOMP가 연결되지 않음. 호가 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/orderbook`;

    // 1) 수신용 SUBSCRIBE
    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        const optimizedData: OptimizedOrderbookData = JSON.parse(message.body);
        const orderbookData = transformOptimizedOrderbookData(optimizedData);
        callback(orderbookData);
      } catch (error) {
        console.error('호가 데이터 파싱 에러:', error, '원본:', message.body);
      }
    });
    this.subscriptions.set(`orderbook-${stockCode}`, subscription);

    // 2) 서버로 구독 요청 SEND (→ @MessageMapping)
    this.client.publish({
      destination: `/app/subscribe/${stockCode}/orderbook`,
      body: '',
    });
  }

  /**
   * 모든 구독 해제 및 연결 종료
   */
  disconnect(): void {
    this.subscriptions.forEach((subscription, key) => {
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
    if (this.client.connected) {
      this.client.deactivate();
    }
    this.isConnected = false;
    console.log(' 연결 해제 완료');
  }

  /**
   * 연결 상태 확인
   */
  getConnectionStatus(): boolean {
    return this.isConnected && this.client.connected;
  }
}
