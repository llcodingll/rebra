import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';

export interface UserProfileResponse {
  userId: number;
  nickname: string;
}

class UserApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  /**
   * 내 정보 조회
   */
  async getProfile(): Promise<Result<UserProfileResponse, AppError>> {
    return this.apiClient.get<UserProfileResponse>('/api/users/me');
  }

  /**
   * 로그아웃
   */
  async logout(): Promise<Result<void, AppError>> {
    return this.apiClient.post<void>('/api/users/logout');
  }
}

export const userApi = new UserApi();