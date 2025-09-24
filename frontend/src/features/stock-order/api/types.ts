/**
 * 주식 주문 API 관련 타입 정의
 */

// 공통 주문 요청 타입 (매수/매도 모두 동일한 구조)
export interface OrderRequest {
  orderType: string; // "00" - 주문 세부 타입 (지정가 등)
  quantity: number;
  price: number;
  accountId: number;
}

// 매수 주문 요청 타입
export interface BuyOrderRequest extends OrderRequest {}

// 매도 주문 요청 타입
export interface SellOrderRequest extends OrderRequest {}

// 주문 응답 데이터 타입
export interface OrderResponseData {
  exchangeOrderNumber: string; // 거래소 주문번호
  orderNumber: string; // 주문번호
  orderTime: string; // 주문시간
  stockCode: string; // 종목코드
  orderType: string; // 주문유형
  quantity: number; // 수량
  price: number; // 가격
  tradeType: string; // 거래유형
  success: boolean; // 성공 여부
  errorMessage: string; // 에러 메시지
}

// 주문 상태 타입
export type OrderStatus = 'idle' | 'loading' | 'success' | 'error';

// 주문 타입 열거형
export const OrderType = {
  LIMIT: '00', // 지정가 주문
} as const;

export type OrderTypeValue = (typeof OrderType)[keyof typeof OrderType];
