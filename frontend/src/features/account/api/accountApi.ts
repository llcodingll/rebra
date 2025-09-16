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
    console.log("registerAccount called with:", requestData);
    return this.apiClient.post<AccountRegisterResponse>('/api/v1/accounts/register', requestData);
  }

  getAccountList = async (): Promise<Result<AccountListResponse, AppError>> => {
    return this.apiClient.get<AccountListResponse>('/api/v1/accounts');
  }
}

export const accountApi = new AccountApi();