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
    }
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
      <div className={styles.priceInput}>
        <label>주문 가격</label>
        <div className={styles.priceControls}>
          <input
            type='number'
            // value={orderPrice}
            onChange={(e) => onPriceChange(Number(e.target.value))}
            className={styles.priceField}
            placeholder='주문 가격을 입력하세요'
          />
          <div className={styles.priceButtons}>
            <button className={styles.priceBtn} onClick={() => onPriceAdjust('up')}>
              +
            </button>
            <button className={styles.priceBtn} onClick={() => onPriceAdjust('down')}>
              -
            </button>
          </div>
        </div>
      </div>

      <div className={styles.quantitySection}>
        <label>수량</label>
        <div className={styles.quantityControls}>
          <input
            type='number'
            value={quantity}
            onChange={(e) => onQuantityChange(Number(e.target.value))}
            className={styles.quantityField}
            placeholder='0'
          />
          <div className={styles.quantityButtons}>
            <button
              className={`${styles.quantityBtn} ${selectedRatio === 10 ? styles.active : ''}`}
              onClick={() => onRatioSelect(10)}
            >
              10%
            </button>
            <button
              className={`${styles.quantityBtn} ${selectedRatio === 25 ? styles.active : ''}`}
              onClick={() => onRatioSelect(25)}
            >
              25%
            </button>
            <button
              className={`${styles.quantityBtn} ${selectedRatio === 50 ? styles.active : ''}`}
              onClick={() => onRatioSelect(50)}
            >
              50%
            </button>
            <button
              className={`${styles.quantityBtn} ${selectedRatio === 100 ? styles.active : ''}`}
              onClick={() => onRatioSelect(100)}
            >
              최대
            </button>
          </div>
        </div>
        <div className={styles.quantityInfo}>
          <span>주문 금액: {formatNumber(orderPrice * quantity)}원</span>
        </div>
      </div>

      <button className={styles.submitButton} onClick={handleOrderSubmit} disabled={quantity <= 0 || sellOrder.isLoading}>
        {sellOrder.isLoading ? '주문 중...' : '판매하기'}
      </button>
    </div>
  );
}
