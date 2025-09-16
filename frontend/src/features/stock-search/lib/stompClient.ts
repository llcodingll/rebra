import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import type {
  KRXRealtimePriceMessage,
  KRXRealtimeOrderbookMessage,
  RealtimePriceMessage,
  RealtimeOrderbookMessage,
} from '../api/types';
import { transformKRXPriceData, transformKRXOrderbookData } from '../utils/krxDataTransform';

export class StockStompClient {
  private client: Client;
  private isConnected = false;
  private subscriptions: Map<string, StompSubscription> = new Map();

  constructor() {
    // WebSocket endpoint 준비
    const wsEndpoint = import.meta.env.VITE_API_BASE_URL + '/ws';
    console.log('🔌 STOMP 클라이언트 초기화:', wsEndpoint);

    this.client = new Client({
      brokerURL: wsEndpoint,
      connectHeaders: {
        // 필요에 따라 인증 헤더 추가
      },
      debug: (str) => {
        console.log('🔗 STOMP Debug:', str);
      },
      // reconnectDelay: 5000, // 5초 후 재연결
      reconnectDelay: 0, // 5초 후 재연결
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
   * 실시간 가격 정보 구독 (Queue 기반)
   * @param stockCode 종목 코드 (예: "005930")
   * @param callback 가격 데이터 수신 콜백
   */
  subscribePriceData(stockCode: string, callback: (data: RealtimePriceMessage) => void): void {
    if (!this.isConnected) {
      console.warn('⚠️ STOMP가 연결되지 않음. 가격 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/price`;
    console.log('📈 가격 정보 구독 (Queue):', channel);

    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('📊 [STOMP] 실시간 가격 데이터 수신 시작');
        console.log('📊 [STOMP] 채널:', channel);
        console.log('📊 [STOMP] 원본 KRX 데이터:', message.body);

        // KRX 데이터를 기존 인터페이스로 변환
        const krxData: KRXRealtimePriceMessage = JSON.parse(message.body);
        console.log('📊 [STOMP] 파싱된 KRX 데이터:', krxData);

        const priceData = transformKRXPriceData(krxData);
        console.log('📊 [STOMP] 변환된 가격 데이터:', priceData);

        callback(priceData);
        console.log('📊 [STOMP] 가격 데이터 콜백 완료');
      } catch (error) {
        console.error('❌ [STOMP] KRX 가격 데이터 파싱 에러:', error);
        console.error('❌ [STOMP] 원본 메시지:', message.body);
        console.error('❌ [STOMP] 에러 스택:', error instanceof Error ? error.stack : 'Unknown error');
      }
    });

    this.subscriptions.set(`price-${stockCode}`, subscription);
  }

  /**
   * 실시간 호가 정보 구독 (Queue 기반)
   * @param stockCode 종목 코드 (예: "005930")
   * @param callback 호가 데이터 수신 콜백
   */
  subscribeOrderbook(stockCode: string, callback: (data: RealtimeOrderbookMessage) => void): void {
    if (!this.isConnected) {
      console.warn('⚠️ STOMP가 연결되지 않음. 호가 구독 실패');
      return;
    }

    const channel = `/user/queue/stock/${stockCode}/orderbook`;
    console.log('📊 호가 정보 구독 (Queue):', channel);

    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('📈 [STOMP] 실시간 호가 데이터 수신 시작');
        console.log('📈 [STOMP] 채널:', channel);
        console.log('📈 [STOMP] 원본 KRX 호가 데이터:', message.body);

        // KRX 데이터를 기존 인터페이스로 변환
        const krxData: KRXRealtimeOrderbookMessage = JSON.parse(message.body);
        console.log('📈 [STOMP] 파싱된 KRX 호가 데이터:', krxData);

        const orderbookData = transformKRXOrderbookData(krxData);
        console.log('📈 [STOMP] 변환된 호가 데이터:', orderbookData);

        callback(orderbookData);
        console.log('📈 [STOMP] 호가 데이터 콜백 완료');
      } catch (error) {
        console.error('❌ [STOMP] KRX 호가 데이터 파싱 에러:', error);
        console.error('❌ [STOMP] 원본 메시지:', message.body);
        console.error('❌ [STOMP] 에러 스택:', error instanceof Error ? error.stack : 'Unknown error');
      }
    });

    this.subscriptions.set(`orderbook-${stockCode}`, subscription);
  }

  /**
   * 모든 구독 해제 및 연결 종료
   */
  disconnect(): void {
    console.log('🔌 STOMP 연결 해제 시작...');

    // 모든 구독 해제
    this.subscriptions.forEach((subscription, key) => {
      console.log('📤 구독 해제:', key);
      subscription.unsubscribe();
    });
    this.subscriptions.clear();

    // STOMP 연결 해제
    if (this.client.connected) {
      this.client.deactivate();
    }

    this.isConnected = false;
    console.log('✅ STOMP 연결 해제 완료');
  }

  /**
   * 연결 상태 확인
   */
  getConnectionStatus(): boolean {
    return this.isConnected && this.client.connected;
  }

  /**
   * 특정 구독 해제
   * @param stockCode 종목 코드
   * @param type 구독 타입 ('price' | 'orderbook')
   */
  unsubscribe(stockCode: string, type: 'price' | 'orderbook'): void {
    const key = `${type}-${stockCode}`;
    const subscription = this.subscriptions.get(key);

    if (subscription) {
      console.log('📤 개별 구독 해제:', key);
      subscription.unsubscribe();
      this.subscriptions.delete(key);
    }
  }
}
