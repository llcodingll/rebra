import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';

export interface AccountRegisterRequest {
  accountNumber: string;
  appKey: string;
  appSecret: string;
  accountType: 'MOCK' | 'REAL';
}

export interface AccountRegisterResponse {
  accountId: number;
  accountNumber: string;
  accountType: 'MOCK' | 'REAL';
  brokerName: string;
  registeredAt: string;
  connected: boolean;
}

export interface AccountItem {
  accountId: number;
  accountNumber: string;
  accountType: string;
  brokerName: string;
  registeredAt: string;
  connected: boolean;
}

export interface AccountListResponse {
  accounts: AccountItem[];
  totalCount: number;
}

class AccountApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  registerAccount = async (requestData: AccountRegisterRequest): Promise<Result<AccountRegisterResponse, AppError>> => {
    console.log("=== 계좌 등록 API 요청 시작 ===");
    console.log("요청 데이터:", JSON.stringify(requestData, null, 2));
    console.log("요청 URL:", '/api/v1/accounts/register');
    console.log("요청 시간:", new Date().toISOString());

    try {
      const result = await this.apiClient.post<AccountRegisterResponse>('/api/v1/accounts/register', requestData);
      console.log("=== 계좌 등록 API 응답 ===");
      console.log("응답 결과:", JSON.stringify(result, null, 2));
      console.log("응답 시간:", new Date().toISOString());
      return result;
    } catch (error) {
      console.error("=== 계좌 등록 API 에러 ===");
      console.error("에러 상세:", error);
      console.error("에러 시간:", new Date().toISOString());
      throw error;
    }
  }

  getAccountList = async (): Promise<Result<AccountListResponse, AppError>> => {
    return this.apiClient.get<AccountListResponse>('/api/v1/accounts');
  }
}

export const accountApi = new AccountApi();