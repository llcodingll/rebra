import { useState } from 'react';
import styles from './BuyOrderForm.module.css';
import { useBuyOrder } from '../hooks/useBuyOrder';

interface BuyOrderFormProps {
  stockCode: string;
  orderPrice: number;
  onPriceChange: (price: number) => void;
  onPriceAdjust: (direction: 'up' | 'down') => void;
  onQuantityChange: (quantity: number) => void;
  onRatioSelect: (ratio: number) => void;
  quantity: number;
  selectedRatio: number | null;
}

export default function BuyOrderForm({
  stockCode,
  orderPrice,
  onPriceChange,
  onPriceAdjust,
  onQuantityChange,
  onRatioSelect,
  quantity,
  selectedRatio,
}: BuyOrderFormProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  // 임시 데이터 (실제로는 props나 API에서 받아야 함)
  const currentHoldings = 100; // 현재 보유 수량
  const currentAvgPrice = 450; // 현재 평균 단가
  const availableCash = 2052; // 구매가능 금액

  // 계산 값들
  const totalOrderAmount = orderPrice * quantity;
  const expectedAvgPrice =
    currentHoldings > 0 && quantity > 0
      ? (currentHoldings * currentAvgPrice + totalOrderAmount) / (currentHoldings + quantity)
      : 0;

  // 매수 주문 훅
  const buyOrder = useBuyOrder({
    stockCode,
    onSuccess: (data) => {
      console.log('✅ 매수 주문 성공:', data);
      alert(`매수 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);
      // 성공 후 폼 초기화
      onQuantityChange(0);
      onRatioSelect(0);
    },
    onError: (error) => {
      console.error('❌ 매수 주문 실패:', error);
      alert(`매수 주문에 실패했습니다: ${error}`);
    },
  });

  const handleOrderSubmit = () => {
    if (quantity <= 0) {
      alert('수량을 입력해주세요.');
      return;
    }

    if (orderPrice <= 0) {
      alert('주문 가격을 입력해주세요.');
      return;
    }

    buyOrder.buyStock({
      quantity,
      price: orderPrice,
    });
  };

  return (
    <div className={styles.orderForm}>
      <div className={styles.priceSection}>
        <label className={styles.sectionLabel}>구매 가격</label>
        <div className={styles.priceControls}>
          <div className={styles.priceInputWrapper}>
            <input
              type='number'
              value={orderPrice || ''}
              onChange={(e) => onPriceChange(Number(e.target.value))}
              className={styles.priceField}
            />
            <span className={styles.priceUnit}>원</span>
          </div>
          <div className={styles.priceButtons}>
            <button className={styles.priceBtn} onClick={() => onPriceAdjust('down')}>
              -
            </button>
            <button className={styles.priceBtn} onClick={() => onPriceAdjust('up')}>
              +
            </button>
          </div>
        </div>
      </div>

      <div className={styles.quantitySection}>
        <label className={styles.sectionLabel}>수량</label>
        <div className={styles.quantityControls}>
          <div className={styles.quantityInputRow}>
            <div className={styles.quantityInputWrapper}>
              <input
                type='number'
                value={quantity}
                onChange={(e) => onQuantityChange(Number(e.target.value))}
                className={styles.quantityField}
                placeholder='0'
              />
              <span className={styles.quantityUnit}>주</span>
            </div>
            <div className={styles.quantityButtons}>
              <button className={styles.quantityAdjustBtn} onClick={() => onQuantityChange(Math.max(0, quantity - 1))}>
                -
              </button>
              <button className={styles.quantityAdjustBtn} onClick={() => onQuantityChange(quantity + 1)}>
                +
              </button>
            </div>
          </div>
          <div className={styles.ratioButtons}>
            <button className={styles.ratioBtn} onClick={() => onRatioSelect(10)}>
              10%
            </button>
            <button className={styles.ratioBtn} onClick={() => onRatioSelect(25)}>
              25%
            </button>
            <button className={styles.ratioBtn} onClick={() => onRatioSelect(50)}>
              50%
            </button>
            <button className={styles.ratioBtn} onClick={() => onRatioSelect(100)}>
              최대
            </button>
          </div>
        </div>
      </div>

      {/* 정보 영역 */}
      <div className={styles.infoSection}>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>내 주식 평균</span>
          <span className={styles.infoValue}>{formatNumber(currentAvgPrice)}원</span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>구매 후 예상</span>
          <span className={styles.infoValue}>
            {expectedAvgPrice > 0 ? `${formatNumber(Math.round(expectedAvgPrice))}원` : '-'}
          </span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>현재 수익</span>
          <span className={`${styles.infoValue} ${styles.negative}`}>-26원 (5.4%)</span>
        </div>
      </div>

      <button
        className={styles.submitButton}
        onClick={handleOrderSubmit}
        // disabled={quantity <= 0 || buyOrder.isLoading}
      >
        {buyOrder.isLoading ? '주문 중...' : '구매하기'}
      </button>
    </div>
  );
}
