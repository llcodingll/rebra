import { useState } from 'react';
import styles from './SellOrderForm.module.css';
import { useSellOrder } from '../hooks/useSellOrder';

interface SellOrderFormProps {
  stockCode: string;
  orderPrice: number;
  onPriceChange: (price: number) => void;
  onPriceAdjust: (direction: 'up' | 'down') => void;
  onQuantityChange: (quantity: number) => void;
  onRatioSelect: (ratio: number) => void;
  quantity: number;
  selectedRatio: number | null;
}

export default function SellOrderForm({
  stockCode,
  orderPrice,
  onPriceChange,
  onPriceAdjust,
  onQuantityChange,
  onRatioSelect,
  quantity,
  selectedRatio,
}: SellOrderFormProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  // 임시 데이터 (실제로는 props나 API에서 받아야 함)
  const currentHoldings = 100; // 현재 보유 수량
  const currentAvgPrice = 450; // 현재 평균 단가
  const availableQuantity = 100; // 판매 가능 수량

  // 계산 값들
  const totalSellAmount = orderPrice * quantity;
  const remainingHoldings = currentHoldings - quantity;
  const expectedAvgPrice =
    remainingHoldings > 0 && quantity > 0 && quantity < currentHoldings
      ? currentAvgPrice // 일부 판매 시 평균 단가는 동일
      : 0; // 전량 판매 시 0

  // 매도 주문 훅
  const sellOrder = useSellOrder({
    stockCode,
    onSuccess: (data) => {
      console.log('✅ 매도 주문 성공:', data);
      alert(`매도 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);
      onQuantityChange(0);
      onRatioSelect(0);
    },
    onError: (error) => {
      console.error('❌ 매도 주문 실패:', error);
      alert(`매도 주문에 실패했습니다: ${error}`);
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

    sellOrder.sellStock({
      quantity,
      price: orderPrice,
    });
  };

  return (
    <div className={styles.orderForm}>
      <div className={styles.priceSection}>
        <label className={styles.sectionLabel}>판매 가격</label>
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
          <span className={styles.infoLabel}>판매 후 예상</span>
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
        disabled={quantity <= 0 || sellOrder.isLoading}
      >
        {sellOrder.isLoading ? '주문 중...' : '판매하기'}
      </button>
    </div>
  );
}
