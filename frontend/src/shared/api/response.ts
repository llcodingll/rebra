export interface ApiResponse<T = unknown> {
  success: boolean;
  status: number;
  data: T;
  errorCode: string;
  errorMessage: string;
  errorData: string;
  timestamp: string;
}
