import { Client, IMessage, StompSubscription } from '@stomp/stompjs';
import type { RealtimePriceMessage, RealtimeOrderbookMessage } from '../api/types';

export class StockStompClient {
  private client: Client;
  private isConnected = false;
  private subscriptions: Map<string, StompSubscription> = new Map();

  constructor(endpoint: string) {
    // WebSocket endpoint 준비
    const wsEndpoint = endpoint.startsWith('ws://') || endpoint.startsWith('wss://')
      ? endpoint
      : `ws://${endpoint}`;

    console.log('🔌 STOMP 클라이언트 초기화:', wsEndpoint);

    this.client = new Client({
      brokerURL: wsEndpoint,
      connectHeaders: {
        // 필요에 따라 인증 헤더 추가
      },
      debug: (str) => {
        console.log('🔗 STOMP Debug:', str);
      },
      reconnectDelay: 5000, // 5초 후 재연결
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
      }
    });
  }

  /**
   * STOMP 서버에 연결
   */
  async connect(): Promise<void> {
    return new Promise((resolve, reject) => {
      console.log('🚀 STOMP 연결 시작...');

      this.client.onConnect = () => {
        console.log('✅ STOMP 연결 완료');
        this.isConnected = true;
        resolve();
      };

      this.client.onStompError = (frame) => {
        console.error('❌ STOMP 연결 실패:', frame);
        this.isConnected = false;
        reject(new Error(`STOMP 연결 실패: ${frame.headers['message']}`));
      };

      this.client.activate();
    });
  }

  /**
   * 실시간 가격 정보 구독
   * @param channel 가격 정보 채널 (예: "/topic/price/005930")
   * @param callback 가격 데이터 수신 콜백
   */
  subscribePriceData(channel: string, callback: (data: RealtimePriceMessage) => void): void {
    if (!this.isConnected) {
      console.warn('⚠️ STOMP가 연결되지 않음. 가격 구독 실패');
      return;
    }

    console.log('📈 가격 정보 구독:', channel);

    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('💰 실시간 가격 데이터 수신:', message.body);

        // 서버에서 받은 메시지 파싱
        const priceData: RealtimePriceMessage = JSON.parse(message.body);
        callback(priceData);
      } catch (error) {
        console.error('❌ 가격 데이터 파싱 에러:', error);
        console.error('❌ 원본 메시지:', message.body);
      }
    });

    this.subscriptions.set(`price-${channel}`, subscription);
  }

  /**
   * 실시간 호가 정보 구독
   * @param channel 호가 정보 채널 (예: "/topic/orderbook/005930")
   * @param callback 호가 데이터 수신 콜백
   */
  subscribeOrderbook(channel: string, callback: (data: RealtimeOrderbookMessage) => void): void {
    if (!this.isConnected) {
      console.warn('⚠️ STOMP가 연결되지 않음. 호가 구독 실패');
      return;
    }

    console.log('📊 호가 정보 구독:', channel);

    const subscription = this.client.subscribe(channel, (message: IMessage) => {
      try {
        console.log('📈 실시간 호가 데이터 수신:', message.body);

        // 서버에서 받은 메시지 파싱
        const orderbookData: RealtimeOrderbookMessage = JSON.parse(message.body);
        callback(orderbookData);
      } catch (error) {
        console.error('❌ 호가 데이터 파싱 에러:', error);
        console.error('❌ 원본 메시지:', message.body);
      }
    });

    this.subscriptions.set(`orderbook-${channel}`, subscription);
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
   * @param channel 해제할 채널명
   * @param type 구독 타입 ('price' | 'orderbook')
   */
  unsubscribe(channel: string, type: 'price' | 'orderbook'): void {
    const key = `${type}-${channel}`;
    const subscription = this.subscriptions.get(key);

    if (subscription) {
      console.log('📤 개별 구독 해제:', key);
      subscription.unsubscribe();
      this.subscriptions.delete(key);
    }
  }
}