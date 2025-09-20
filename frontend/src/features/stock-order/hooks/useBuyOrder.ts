import { useState, useCallback } from 'react';
import { useMutation } from '@tanstack/react-query';
import { stockOrderApi } from '../api/stockOrderApi';
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

  const mutation = useMutation({
    mutationFn: async (orderData: BuyOrderRequest) => {
      console.log(`🛒 매수 주문 시작: ${stockCode}`, orderData);
      setOrderStatus('loading');

      const result = await stockOrderApi.buyStock(stockCode, orderData);

      if (!result.success) {
        throw new Error(result.error.message || '매수 주문에 실패했습니다');
      }

      return result.data;
    },
    onSuccess: (data) => {
      console.log('✅ 매수 주문 성공:', data);
      setOrderStatus('success');
      onSuccess?.(data);
    },
    onError: (error: Error) => {
      console.error('❌ 매수 주문 실패:', error);
      setOrderStatus('error');
      onError?.(error.message);
    },
    onSettled: () => {
      // 2초 후 상태 초기화
      setTimeout(() => {
        setOrderStatus('idle');
      }, 2000);
    }
  });

  const buyStock = useCallback((orderData: Omit<BuyOrderRequest, 'orderType'>) => {
    const buyOrderData: BuyOrderRequest = {
      ...orderData,
      orderType: '01', // 지정가 주문 타입
    };

    mutation.mutate(buyOrderData);
  }, [mutation]);

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