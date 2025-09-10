import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';

export interface SignupRequest {
  nickname: string;
  age: number;
  mainIncomeSource: string;
  investmentPurpose: number;
  investmentExperience: number;
  riskTolerance: number;
}

export interface SignupResponse {
  userId: number;
  nickname: string;
  message: string;
}

export interface NicknameCheckResponse {
  isDuplicated: boolean;
}

class AuthApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  async signup(data: SignupRequest): Promise<Result<SignupResponse, AppError>> {
    return this.apiClient.post<SignupResponse>('/auth/signup', data);
  }

  async checkNickname(nickname: string): Promise<Result<NicknameCheckResponse, AppError>> {
    return this.apiClient.get<NicknameCheckResponse>(`/auth/nickname/check?nickname=${encodeURIComponent(nickname)}`);
  }
}

export const authApi = new AuthApi();