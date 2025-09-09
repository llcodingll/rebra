import axios from 'axios';
import type { KisTokenRequest, KisTokenResponse, KisWebSocketApprovalRequest, KisWebSocketApprovalResponse } from './types';

export class KisClient {
  private static readonly BASE_URL = 'https://openapi.koreainvestment.com:9443';
  private static readonly WS_URL = 'ws://ops.koreainvestment.com:21000';
  
  private appkey: string;
  private appsecret: string;
  private accessToken?: string;
  private tokenExpiresAt?: number;

  constructor(appkey: string, appsecret: string) {
    this.appkey = appkey;
    this.appsecret = appsecret;
  }

  private isTokenValid(): boolean {
    if (!this.accessToken || !this.tokenExpiresAt) {
      return false;
    }
    return Date.now() < this.tokenExpiresAt;
  }

  async getAccessToken(): Promise<string> {
    if (this.isTokenValid()) {
      return this.accessToken!;
    }

    const request: KisTokenRequest = {
      grant_type: 'client_credentials',
      appkey: this.appkey,
      appsecret: this.appsecret
    };

    try {
      const response = await axios.post<KisTokenResponse>(
        `${KisClient.BASE_URL}/oauth2/tokenP`,
        request,
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
      );

      this.accessToken = response.data.access_token;
      this.tokenExpiresAt = Date.now() + (response.data.expires_in * 1000);
      
      return this.accessToken;
    } catch (error) {
      console.error('Failed to get KIS access token:', error);
      throw new Error('토큰 발급에 실패했습니다.');
    }
  }

  async getWebSocketApproval(stockCode: string): Promise<string> {
    const accessToken = await this.getAccessToken();
    
    const request: KisWebSocketApprovalRequest = {
      custtype: 'P',
      tr_type: '1',
      content: {
        tr_id: 'H0STCNT0',
        tr_key: stockCode
      }
    };

    try {
      const response = await axios.post<KisWebSocketApprovalResponse>(
        `${KisClient.BASE_URL}/tryitout/H0STCNT0`,
        request,
        {
          headers: {
            'Content-Type': 'application/json',
            'authorization': `Bearer ${accessToken}`,
            'appkey': this.appkey,
            'appsecret': this.appsecret,
            'tr_id': 'H0STCNT0',
            'custtype': 'P'
          }
        }
      );

      if (response.data.body.rt_cd !== '0') {
        throw new Error(`웹소켓 승인 실패: ${response.data.body.msg1}`);
      }

      return 'approved';
    } catch (error) {
      console.error('Failed to get WebSocket approval:', error);
      throw new Error('웹소켓 승인에 실패했습니다.');
    }
  }

  getWebSocketUrl(): string {
    return KisClient.WS_URL;
  }

  getAuthHeaders() {
    return {
      appkey: this.appkey,
      appsecret: this.appsecret
    };
  }
}