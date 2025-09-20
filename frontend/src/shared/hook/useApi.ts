import { useQuery, useMutation } from '@tanstack/react-query';
import type { UseQueryOptions, UseMutationOptions } from '@tanstack/react-query';
import { isOk, type Result } from '../util/result';
import type { AppError } from '../util/appErrors';
import { ErrorProcessor } from '../util/errorProcessor';

// Result 패턴용 API 함수 타입
type ApiFunction<TData, TVariables = void> = (variables: TVariables) => Promise<Result<TData, AppError>>;

// QueryKey 타입 (간단하게 정의)
type QueryKey = readonly unknown[];

// useApi 훅 옵션 타입 - Result 패턴 최적화
interface UseApiOptions<TData, TVariables = void>
  extends Omit<UseQueryOptions<TData, AppError, TData, QueryKey>, 'queryKey' | 'queryFn'> {
  queryKey: QueryKey;
  apiFunction: ApiFunction<TData, TVariables>;
  variables?: TVariables;
  errorMessages?: {
    [statusCode: number]: string;
  } | string; // 전체 에러 메시지 오버라이드 또는 상태별 메시지
}

/**
 * Result 패턴과 통합된 useQuery 래퍼
 * - 자동 에러 처리 (ErrorProcessor가 이미 처리함)
 * - 깔끔한 타입 안전성
 * - 성공 데이터만 자동 추출
 */
export const useApi = <TData, TVariables = void>({
  queryKey,
  apiFunction,
  variables,
  errorMessages,
  ...options
}: UseApiOptions<TData, TVariables>) => {
  return useQuery<TData, AppError, TData, QueryKey>({
    queryKey,
    queryFn: async () => {
      const result = await apiFunction(variables as TVariables);

      if (isOk(result)) {
        return result.data; // 성공 데이터만 반환
      } else {
        // 에러 타입에 따라 적절히 처리
        const error = result.error;

        if (error && typeof error === 'object' && 'isAxiosError' in error) {
          // AxiosError인 경우: 커스텀 메시지 또는 기본 메시지로 ErrorProcessor 호출
          const processedError = ErrorProcessor.processAxiosError(error as any, errorMessages);
          throw processedError;
        } else {
          // 이미 처리된 AppError인 경우: 그대로 throw
          throw error;
        }
      }
    },
    ...options,
  });
};

// API 뮤테이션 훅 옵션 타입
interface UseApiMutationOptions<TData, TVariables = void>
  extends Omit<UseMutationOptions<TData, AppError, TVariables>, 'mutationFn'> {
  apiFunction: ApiFunction<TData, TVariables>;
}

/**
 * Result 패턴과 통합된 useMutation 래퍼
 * - 자동 에러 처리 (ErrorProcessor가 이미 처리함)
 * - 타입 안전한 뮤테이션
 * - 성공 데이터만 자동 추출
 */
export const useApiMutation = <TData, TVariables = void>({
  apiFunction,
  ...options
}: UseApiMutationOptions<TData, TVariables>) => {
  return useMutation<TData, AppError, TVariables>({
    mutationFn: async (variables: TVariables) => {
      const result = await apiFunction(variables);

      if (isOk(result)) {
        return result.data; // 성공 데이터만 반환
      } else {
        // AppError를 던져서 React Query 에러 처리
        // ErrorProcessor에서 이미 사용자 알림 처리됨
        throw result.error;
      }
    },
    ...options,
  });
};

// 편의용 타입 export
export type { ApiFunction, UseApiOptions, UseApiMutationOptions };
