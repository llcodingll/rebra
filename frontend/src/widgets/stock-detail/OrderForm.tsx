import { useState } from 'react';
import styles from './OrderForm.module.css';

interface OrderFormProps {
  orderPrice: number;
  onPriceAdjust: (direction: 'up' | 'down') => void;
  onQuantityChange: (quantity: number) => void;
  onRatioSelect: (ratio: number) => void;
  onOrderSubmit: () => void;
  quantity: number;
  selectedRatio: number | null;
}

export default function OrderForm({
  orderPrice,
  onPriceAdjust,
  onQuantityChange,
  onRatioSelect,
  onOrderSubmit,
  quantity,
  selectedRatio
}: OrderFormProps) {
  const [activeTab, setActiveTab] = useState<'구매' | '판매' | '대기'>('구매');

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  return (
    <div className={styles.orderSection}>
      <div className={styles.orderTabs}>
        {(['구매', '판매', '대기'] as const).map((tab) => (
          <button
            key={tab}
            className={`${styles.orderTab} ${activeTab === tab ? styles.active : ''}`}
            onClick={() => setActiveTab(tab)}
          >
            {tab}
          </button>
        ))}
      </div>
      
      <div className={styles.orderForm}>
        <div className={styles.priceInput}>
          <label>주문 가격</label>
          <div className={styles.priceControls}>
            <input
              type="text"
              value={formatNumber(orderPrice)}
              readOnly
              className={styles.priceField}
            />
            <div className={styles.priceButtons}>
              <button 
                className={styles.priceBtn}
                onClick={() => onPriceAdjust('up')}
              >
                +
              </button>
              <button 
                className={styles.priceBtn}
                onClick={() => onPriceAdjust('down')}
              >
                -
              </button>
            </div>
          </div>
        </div>

        <div className={styles.quantitySection}>
          <label>수량</label>
          <div className={styles.quantityControls}>
            <input
              type="number"
              value={quantity}
              onChange={(e) => onQuantityChange(Number(e.target.value))}
              className={styles.quantityField}
              placeholder="0"
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

        <button 
          className={styles.submitButton}
          onClick={onOrderSubmit}
          disabled={quantity <= 0}
        >
          {activeTab}하기
        </button>
      </div>
    </div>
  );
}