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
    console.log('🔌 STOMP 클라이언트 초기화:', wsEndpoint);

    this.client = new Client({
      brokerURL: wsEndpoint,
      connectHeaders: {
        // 필요 시 인증 헤더 추가
      },
      debug: (str) => {
        console.log('🔗 STOMP Debug:', str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,

      onConnect: () => {
        console.log('✅ STOMP 연결 성공');
        this.isConnected = true;
      },

      onStompError: (frame) => {
        console.error('❌ STOMP 에러:', frame.headers['message']);
        console.error('❌ STOMP 에러 상세:', frame.body);
        this.isConnected = false;
      },

      onWebSocketError: (error) => {
        console.error('❌ WebSocket 에러:', error);
        this.isConnected = false;
      },

      onDisconnect: () => {
        console.log('🔌 STOMP 연결 해제됨');
        this.isConnected = false;
      },
    });
  }

  /**
   * STOMP 서버에 연결
   */
  async connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      console.log('🚀 [STOMP] 연결 시작...');
      console.log('🚀 [STOMP] 대상 엔드포인트:', this.client.brokerURL);

      this.client.onConnect = () => {
        console.log('✅ [STOMP] 연결 완료');
        console.log('✅ [STOMP] 연결된 브로커:', this.client.brokerURL);
        this.isConnected = true;
        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error('❌ [STOMP] 연결 실패');
        console.error('❌ [STOMP] 에러 헤더:', frame.headers);
        console.error('❌ [STOMP] 에러 본문:', frame.body);
        this.isConnected = false;
        reject(new Error(`STOMP 연결 실패: ${frame.headers['message'] || '알 수 없는 오류'}`));
      };

      this.client.onWebSocketError = (error) => {
        console.error('❌ [STOMP] WebSocket 에러:', error);
      };

      console.log('🚀 [STOMP] 클라이언트 활성화...');
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
      console.warn('⚠️ STOMP가 연결되지 않음. 가격 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/price`;
    console.log('📈 가격 정보 구독 (Queue):', channel);

    // 1) 수신용 SUBSCRIBE
    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('📊 [STOMP] 실시간 가격 데이터 수신 시작');
        const optimizedData: OptimizedPriceData = JSON.parse(message.body);
        const priceData = transformOptimizedPriceData(optimizedData);
        callback(priceData);
      } catch (error) {
        console.error('❌ [STOMP] 가격 데이터 파싱 에러:', error, '원본:', message.body);
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
      console.warn('⚠️ STOMP가 연결되지 않음. 호가 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/orderbook`;
    console.log('📊 호가 정보 구독 (Queue):', channel);

    // 1) 수신용 SUBSCRIBE
    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('📈 [STOMP] 실시간 호가 데이터 수신 시작');
        const optimizedData: OptimizedOrderbookData = JSON.parse(message.body);
        const orderbookData = transformOptimizedOrderbookData(optimizedData);
        callback(orderbookData);
      } catch (error) {
        console.error('❌ [STOMP] 호가 데이터 파싱 에러:', error, '원본:', message.body);
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
    console.log('🔌 STOMP 연결 해제 시작...');
    this.subscriptions.forEach((subscription, key) => {
      console.log('📤 구독 해제:', key);
      subscription.unsubscribe();
    });
    this.subscriptions.clear();
    if (this.client.connected) {
      this.client.deactivate();
    }
    this.isConnected = false;
    console.log('✅ STOMP 연결 해제 완료');
  }

  /**
   * 특정 구독 해제
   */
  unsubscribe(stockCode: string, type: 'price' | 'orderbook'): void {
    const key = `${type}-${stockCode}`;
    const subscription = this.subscriptions.get(key);
    if (subscription) {
      console.log('📤 개별 구독 해제:', key);
      subscription.unsubscribe();
      this.subscriptions.delete(key);

      // 서버에도 구독 해제 요청 SEND
      this.client.publish({
        destination: `/app/unsubscribe/${stockCode}`,
        body: type,
      });
    }
  }

  /**
   * 연결 상태 확인
   */
  getConnectionStatus(): boolean {
    return this.isConnected && this.client.connected;
  }
}
