import { useState, useCallback } from 'react';
import { useMutation } from '@tanstack/react-query';
import { stockOrderApi } from '../api/stockOrderApi';
import { useAccountStore } from '../../../entities/account/accountStore';
import type { BuyOrderRequest, OrderResponseData, OrderStatus } from '../api/types';

interface UseBuyOrderProps {
  stockCode: string;
  onSuccess?: (data: OrderResponseData) => void;
  onError?: (error: string) => void;
}

/**
 * 매수 주문 훅
 * @param stockCode 종목코드
 * @param onSuccess 성공 콜백
 * @param onError 에러 콜백
 * @returns 매수 주문 관련 상태와 함수들
 */
export const useBuyOrder = ({ stockCode, onSuccess, onError }: UseBuyOrderProps) => {
  const [orderStatus, setOrderStatus] = useState<OrderStatus>('idle');
  const { accountId } = useAccountStore();

  const mutation = useMutation({
    mutationFn: async (orderData: BuyOrderRequest) => {
      setOrderStatus('loading');

      const result = await stockOrderApi.buyStock(stockCode, orderData);

      if (!result.success) {
        throw new Error(result.error.message || '매수 주문에 실패했습니다');
      }

      return result.data;
    },
    onSuccess: (data) => {
      setOrderStatus('success');
      onSuccess?.(data);
    },
    onError: (error: Error) => {
      setOrderStatus('error');
      onError?.(error.message);
    },
    onSettled: () => {
      // 2초 후 상태 초기화
      setTimeout(() => {
        setOrderStatus('idle');
      }, 2000);
    },
  });

  const buyStock = useCallback(
    (orderData: Omit<BuyOrderRequest, 'orderType' | 'accountId'>) => {
      if (!accountId) {
        onError?.('계정 정보를 찾을 수 없습니다. 대시보드에서 포트폴리오를 선택해주세요.');
        return;
      }

      const buyOrderData: BuyOrderRequest = {
        ...orderData,
        accountId,
        orderType: '00', // 지정가 주문 타입
      };

      mutation.mutate(buyOrderData);
    },
    [mutation, accountId, onError]
  );

  return {
    buyStock,
    isLoading: mutation.isPending,
    isSuccess: mutation.isSuccess,
    isError: mutation.isError,
    error: mutation.error?.message,
    data: mutation.data,
    orderStatus,
    reset: mutation.reset,
  };
};
