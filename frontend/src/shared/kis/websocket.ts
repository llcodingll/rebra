import type { KisRealTimeData } from './types';
import type { KisClient } from './kisClient';

export interface WebSocketDataHandler {
  (data: KisRealTimeData): void;
}

export class KisWebSocket {
  private ws?: WebSocket;
  private kisClient: KisClient;
  private stockCode: string;
  private dataHandler?: WebSocketDataHandler;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 3000;

  constructor(kisClient: KisClient, stockCode: string) {
    this.kisClient = kisClient;
    this.stockCode = stockCode;
  }

  async connect(dataHandler: WebSocketDataHandler): Promise<void> {
    this.dataHandler = dataHandler;

    try {
      // 웹소켓 승인 받기
      await this.kisClient.getWebSocketApproval(this.stockCode);
      
      const wsUrl = this.kisClient.getWebSocketUrl();
      this.ws = new WebSocket(wsUrl);
      
      this.setupEventHandlers();
      
      return new Promise((resolve, reject) => {
        if (!this.ws) return reject(new Error('WebSocket not initialized'));
        
        this.ws.onopen = () => {
          console.log('한국투자증권 웹소켓 연결됨');
          this.reconnectAttempts = 0;
          this.sendSubscriptionRequest();
          resolve();
        };
        
        this.ws.onerror = (error) => {
          console.error('웹소켓 연결 에러:', error);
          reject(error);
        };
      });
    } catch (error) {
      console.error('웹소켓 연결 실패:', error);
      throw error;
    }
  }

  private setupEventHandlers(): void {
    if (!this.ws) return;

    this.ws.onmessage = (event) => {
      try {
        const data: KisRealTimeData = JSON.parse(event.data);
        
        if (data.body.rt_cd === '0' && this.dataHandler) {
          this.dataHandler(data);
        } else if (data.body.rt_cd !== '0') {
          console.warn('KIS API 에러:', data.body.msg1);
        }
      } catch (error) {
        console.error('웹소켓 메시지 파싱 에러:', error);
      }
    };

    this.ws.onclose = (event) => {
      console.log('웹소켓 연결 종료:', event.code, event.reason);
      
      if (this.reconnectAttempts < this.maxReconnectAttempts) {
        this.reconnectAttempts++;
        console.log(`재연결 시도 ${this.reconnectAttempts}/${this.maxReconnectAttempts}`);
        
        setTimeout(() => {
          if (this.dataHandler) {
            this.connect(this.dataHandler);
          }
        }, this.reconnectDelay);
      }
    };

    this.ws.onerror = (error) => {
      console.error('웹소켓 에러:', error);
    };
  }

  private sendSubscriptionRequest(): void {
    if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
      console.warn('웹소켓이 연결되지 않음');
      return;
    }

    const authHeaders = this.kisClient.getAuthHeaders();
    
    const subscriptionMessage = {
      header: {
        approval_key: '',
        custtype: 'P',
        tr_type: '1',
        content_type: 'utf-8'
      },
      body: {
        input: {
          tr_id: 'H0STCNT0',
          tr_key: this.stockCode
        }
      }
    };

    console.log('구독 요청 전송:', this.stockCode);
    this.ws.send(JSON.stringify(subscriptionMessage));
  }

  disconnect(): void {
    if (this.ws) {
      this.ws.close();
      this.ws = undefined;
      this.dataHandler = undefined;
      console.log('웹소켓 연결 해제됨');
    }
  }

  isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN;
  }
}