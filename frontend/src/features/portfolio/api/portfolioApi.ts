import { ApiClient } from '../../../shared/api/apiClient';
import type { Result } from '../../../shared/util/result';
import type { AppError } from '../../../shared/util/appErrors';
import type {
  PortfolioItem,
  PortfolioListResponse,
  PortfolioCreateRequest,
  PortfolioCreateResponse,
  PortfolioDetailResponse,
  StockRegisterRequest,
  StockRegisterResponse,
  StockDeleteRequest,
  StockDeleteResponse,
  StockUpdateRequest,
  StockUpdateResponse
} from './types';

class PortfolioApi {
  private apiClient: ApiClient;

  constructor() {
    this.apiClient = new ApiClient();
  }

  getPortfolioList = async (): Promise<Result<PortfolioListResponse, AppError>> => {
    return this.apiClient.get<PortfolioListResponse>('/api/v1/portfolios');
  }

  createPortfolio = async (requestData: PortfolioCreateRequest): Promise<Result<PortfolioCreateResponse, AppError>> => {
    return this.apiClient.post<PortfolioCreateResponse>('/api/v1/portfolios', requestData);
  }

  getPortfolioDetail = async (portfolioId: number): Promise<Result<PortfolioDetailResponse, AppError>> => {
    console.log("=== 포트폴리오 상세 조회 API 요청 ===");
    console.log("포트폴리오 ID:", portfolioId);
    console.log("요청 URL:", `/api/v1/portfolios/${portfolioId}`);

    return this.apiClient.get<PortfolioDetailResponse>(`/api/v1/portfolios/${portfolioId}`);
  }

  registerStock = async (portfolioId: number, requestData: StockRegisterRequest): Promise<Result<StockRegisterResponse, AppError>> => {
    console.log("=== 주식 등록 API 요청 ===");
    console.log("포트폴리오 ID:", portfolioId);
    console.log("요청 데이터:", requestData);
    console.log("요청 URL:", `/api/v1/portfolios/${portfolioId}/stocks`);

    return this.apiClient.post<StockRegisterResponse>(`/api/v1/portfolios/${portfolioId}/stocks`, requestData);
  }

  deleteStock = async (portfolioId: number, requestData: StockDeleteRequest): Promise<Result<StockDeleteResponse, AppError>> => {
    console.log("=== 주식 삭제 API 요청 ===");
    console.log("포트폴리오 ID:", portfolioId);
    console.log("요청 데이터:", requestData);
    console.log("요청 URL:", `/api/v1/portfolios/${portfolioId}/stocks`);

    return this.apiClient.delete<StockDeleteResponse>(`/api/v1/portfolios/${portfolioId}/stocks`, requestData);
  }

  updateStocks = async (portfolioId: number, requestData: StockUpdateRequest): Promise<Result<StockUpdateResponse, AppError>> => {
    console.log("=== 주식 설정 업데이트 API 요청 ===");
    console.log("포트폴리오 ID:", portfolioId);
    console.log("요청 데이터:", requestData);
    console.log("요청 URL:", `/api/v1/portfolios/${portfolioId}/stocks/batch`);

    return this.apiClient.put<StockUpdateResponse>(`/api/v1/portfolios/${portfolioId}/stocks/batch`, requestData);
  }
}

export const portfolioApi = new PortfolioApi();