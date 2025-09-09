export interface WebSocketConfig {
  url: string;
  key?: string;
  reconnectInterval?: number;
  maxReconnectAttempts?: number;
}

export interface StockData {
  code: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  volume: number;
  timestamp: number;
}

export class WebSocketClient {
  private config: WebSocketConfig;
  private socket: WebSocket | null = null;
  private reconnectCount = 0;
  private isConnecting = false;
  private reconnectTimer?: NodeJS.Timeout;

  constructor(config: WebSocketConfig) {
    this.config = {
      reconnectInterval: 3000,
      maxReconnectAttempts: 5,
      ...config,
    };
  }

  connect(
    onMessage: (data: StockData) => void,
    onStatusChange: (status: 'connecting' | 'connected' | 'disconnected' | 'error') => void
  ): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnecting || (this.socket && this.socket.readyState === WebSocket.CONNECTING)) {
        return;
      }

      this.isConnecting = true;
      onStatusChange('connecting');

      try {
        this.socket = new WebSocket(this.config.url);

        const connectionTimeout = setTimeout(() => {
          if (this.socket && this.socket.readyState === WebSocket.CONNECTING) {
            this.socket.close();
            reject(new Error('Connection timeout'));
          }
        }, 10000);

        this.socket.onopen = () => {
          clearTimeout(connectionTimeout);
          this.isConnecting = false;
          this.reconnectCount = 0;
          onStatusChange('connected');
          console.log('WebSocket connected');
          resolve();
        };

        this.socket.onmessage = (event) => {
          try {
            const data = JSON.parse(event.data);
            onMessage(data);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        };

        this.socket.onclose = (event) => {
          clearTimeout(connectionTimeout);
          this.isConnecting = false;
          onStatusChange('disconnected');
          console.log(`WebSocket closed: ${event.code}`);
          
          if (event.code !== 1000 && this.reconnectCount < (this.config.maxReconnectAttempts || 5)) {
            this.scheduleReconnect(onMessage, onStatusChange);
          }
        };

        this.socket.onerror = (error) => {
          clearTimeout(connectionTimeout);
          this.isConnecting = false;
          onStatusChange('error');
          console.error('WebSocket error:', error);
          reject(error);
        };

      } catch (error) {
        this.isConnecting = false;
        onStatusChange('error');
        reject(error);
      }
    });
  }

  private scheduleReconnect(
    onMessage: (data: StockData) => void,
    onStatusChange: (status: 'connecting' | 'connected' | 'disconnected' | 'error') => void
  ) {
    this.reconnectTimer = setTimeout(() => {
      this.reconnectCount++;
      console.log(`Attempting to reconnect (${this.reconnectCount}/${this.config.maxReconnectAttempts})`);
      this.connect(onMessage, onStatusChange).catch(console.error);
    }, this.config.reconnectInterval);
  }

  disconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }
    
    if (this.socket) {
      this.socket.close(1000);
      this.socket = null;
    }
    
    this.isConnecting = false;
    this.reconnectCount = 0;
  }

  send(data: any) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(data));
    }
  }

  get isConnected(): boolean {
    return this.socket?.readyState === WebSocket.OPEN;
  }

  get status(): string {
    if (!this.socket) return 'disconnected';
    
    switch (this.socket.readyState) {
      case WebSocket.CONNECTING: return 'connecting';
      case WebSocket.OPEN: return 'connected';
      case WebSocket.CLOSING: return 'disconnecting';
      case WebSocket.CLOSED: return 'disconnected';
      default: return 'unknown';
    }
  }
}
