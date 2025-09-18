import axios from 'axios';
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse, AxiosError } from 'axios';
import type { ApiResponse } from './response';
import { getApiConfig } from '../config/apiConfig';
import { retryRequest } from '../util/retry';
import { isOk, wrapAsync, type Result, Ok, Err } from '../util/result';
import { ErrorProcessor } from '../util/errorProcessor';
import type { AppError } from '../util/appErrors';

// CustomAxiosInstance 인터페이스 유지 - ApiResponse<T> 타입 보장
interface CustomAxiosInstance extends AxiosInstance {
  get<T = unknown, R = ApiResponse<T>, D = any>(
    url: string,
    config?: Partial<InternalAxiosRequestConfig<D>>
  ): Promise<R>;

  post<T = unknown, R = ApiResponse<T>, D = any>(
    url: string,
    data?: D,
    config?: InternalAxiosRequestConfig<D>
  ): Promise<R>;

  put<T = unknown, R = ApiResponse<T>, D = any>(
    url: string,
    data?: D,
    config?: InternalAxiosRequestConfig<D>
  ): Promise<R>;

  delete<T = unknown, R = ApiResponse<T>, D = any>(url: string, data?: D, config?: InternalAxiosRequestConfig<D>): Promise<R>;
}

/**
 * API 클라이언트
 * httpOnly 쿠키 기반 인증 사용
 */
export class ApiClient {
  private client: CustomAxiosInstance;
  private onUnauthorizedCallback: (() => Promise<void>) | null = null;

  constructor() {
    const config = getApiConfig();
    this.client = axios.create({
      baseURL: config.baseURL,
      timeout: config.timeout.default,
      headers: config.headers,
      withCredentials: true, // httpOnly 쿠키를 위해 필요
    }) as CustomAxiosInstance;

    this.setupInterceptors();
  }


  // 401 에러 시 호출될 콜백 설정
  setUnauthorizedCallback(callback: () => Promise<void>): void {
    this.onUnauthorizedCallback = callback;
  }

  // 권한 해제할 시 실행될 함수
  private async handleUnauthorized(): Promise<void> {
    if (this.onUnauthorizedCallback) {
      await this.onUnauthorizedCallback();
    }
  }

  private setupInterceptors(): void {
    // 요청 인터셉터 - httpOnly 쿠키는 브라우저가 자동으로 처리하므로 토큰 관련 로직 제거
    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        return config;
      },
      (error: AxiosError) => {
        return Promise.reject(error);
      }
    );

    // 응답 인터셉터 - httpOnly 쿠키 기반이므로 토큰 갱신 로직 제거
    this.client.interceptors.response.use(
      (response: AxiosResponse) => response.data, // ApiResponse<T> 반환
      async (error: AxiosError) => {
        const originalRequest = error.config;

        // 401 에러 시 로그아웃 처리 (httpOnly 쿠키는 서버에서 자동 갱신)
        if (error.response?.status === 401) {
          await this.handleUnauthorized();
          return Promise.reject(error);
        }

        // 서버 에러 - 재시도 로직 적용
        if (error.response?.status && error.response.status >= 500 && error.response.status < 600) {
          const shouldRetry = await retryRequest(originalRequest, error);
          if (shouldRetry && originalRequest) {
            return this.client(originalRequest);
          }
        }

        // 에러는 그대로 throw (wrapApiCall에서 처리)
        return Promise.reject(error);
      }
    );
  }

  // API 호출을 감싸서 Result 패턴으로 변환, 중앙집중식 에러 처리
  private async wrapApiCall<T>(apiCall: () => Promise<ApiResponse<T>>): Promise<Result<T, AppError>> {
    const result = await wrapAsync(apiCall);

    if (isOk(result)) {
      if (result.data.success) {
        return Ok(result.data.data); // 성공 데이터만 반환
      } else {
        // 비즈니스 에러를 중앙에서 처리 (HTTP 200이지만 success: false)
        return Err(ErrorProcessor.processBusinessError(result.data as any));
      }
    } else {
      // HTTP/네트워크 에러는 이미 wrapAsync에서 ErrorProcessor로 처리됨
      return result; // 에러 그대로 반환
    }
  }


  // HTTP 메서드 + Result 패턴
  async get<T>(url: string, config?: InternalAxiosRequestConfig): Promise<Result<T, AppError>> {
    return this.wrapApiCall(() => this.client.get<T>(url, config));
  }

  async post<T>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<Result<T, AppError>> {
    return this.wrapApiCall(() => this.client.post<T>(url, data, config));
  }

  async put<T>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<Result<T, AppError>> {
    return this.wrapApiCall(() => this.client.put<T>(url, data, config));
  }

  async delete<T>(url: string, data?: any, config?: InternalAxiosRequestConfig): Promise<Result<T, AppError>> {
    return this.wrapApiCall(() => this.client.delete<T>(url, { ...config, data }));
  }

}
