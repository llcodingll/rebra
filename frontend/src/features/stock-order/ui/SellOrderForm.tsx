import { useState, useEffect, useCallback } from 'react';
import styles from './SellOrderForm.module.css';
import { useSellOrder } from '../hooks/useSellOrder';
import type { StockHoldingData } from '../../stock-detail/api/types';

interface SellOrderFormProps {
  stockCode: string;
  stockName: string;
  holdingData: StockHoldingData | null;
  currentPrice: number;
  orderBookClickedPrice?: number;
  onRefreshHolding?: () => void;
}

export default function SellOrderForm({
  stockCode,
  stockName,
  holdingData,
  currentPrice,
  orderBookClickedPrice,
  onRefreshHolding,
}: SellOrderFormProps) {
  const [orderPrice, setOrderPrice] = useState(0);
  const [quantity, setQuantity] = useState<number | ''>('');
  const [isPriceInitialized, setIsPriceInitialized] = useState(false);

  // currentPrice가 실제 값으로 변경될 때 1회만 orderPrice 업데이트
  useEffect(() => {
    if (currentPrice > 0 && !isPriceInitialized) {
      setOrderPrice(currentPrice);
      setIsPriceInitialized(true);
    }
  }, [currentPrice, isPriceInitialized]);

  // 호가창에서 클릭된 가격을 주문 가격에 설정
  useEffect(() => {
    if (orderBookClickedPrice && orderBookClickedPrice > 0) {
      setOrderPrice(orderBookClickedPrice);
    }
  }, [orderBookClickedPrice]);
  // const [isQuantityExceeded, setIsQuantityExceeded] = useState(false);
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  // 보유 종목이 없는 경우 처리
  if (!holdingData || holdingData.holdingQuantity === 0) {
    return (
      <div className={styles.orderForm}>
        <div className={styles.emptyState}>
          <div className={styles.emptyMessage}>판매할 주식이 없어요</div>
          <div className={styles.emptySubMessage}>먼저 주식을 구매해주세요.</div>
        </div>
      </div>
    );
  }

  // 현재 보유 정보
  const currentHoldings = holdingData.holdingQuantity;
  const currentAvgPrice = holdingData.averagePurchasePrice;
  const availableQuantity = holdingData.orderableQuantity;

  // console.log(availableQuantity);

  // 계산 값들
  const numericQuantity = typeof quantity === 'number' ? quantity : 0;
  const totalSellAmount = orderPrice * numericQuantity;
  const remainingQuantity = currentHoldings - numericQuantity;
  const realizedProfitLoss = numericQuantity > 0 ? (orderPrice - currentAvgPrice) * numericQuantity : 0;

  // 수량 변경 검증 함수
  const handleQuantityChange = (input: string) => {
    // 빈 값 허용
    if (input === '') {
      setQuantity('');
      return;
    }

    const newQuantity = Number(input);

    // 음수나 NaN 체크
    if (isNaN(newQuantity) || newQuantity < 0) {
      return; // 입력 차단
    }

    // 보유 수량 체크, 초과시 최대값으로 자동 조정 (기존 로직 유지)
    if (newQuantity > currentHoldings) {
      setQuantity(currentHoldings);
      alert('보유 수량을 초과하여 최대 판매 가능 수량으로 조정했습니다');
    } else {
      setQuantity(newQuantity);
    }
  };

  // 가격 조정 함수
  const handlePriceAdjust = (direction: 'up' | 'down') => {
    const step = 100;
    setOrderPrice((prev) => (direction === 'up' ? prev + step : Math.max(prev - step, 0)));
  };

  // 비율 버튼 처리 (보유 수량 기준)
  const handleRatioClick = (ratio: number) => {
    const targetQuantity = Math.floor((currentHoldings * ratio) / 100);
    console.log(targetQuantity);
    setQuantity(targetQuantity);
  };

  // 매도 주문 훅
  const sellOrder = useSellOrder({
    stockCode,
    stockName,
    onSuccess: async (data) => {
      try {
        // 1. 먼저 보유 정보 갱신
        await onRefreshHolding?.();

        // 2. 갱신 완료 후 alert 표시
        alert(`매도 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);

        // 3. 폼 초기화
        setQuantity('');
      } catch (error) {
        // refetch 실패 시에도 alert 표시
        alert(`매도 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);
        setQuantity('');
      }
    },
    onError: (error) => {
      alert(`매도 주문에 실패했습니다: ${error}`);
    },
  });

  const handleOrderSubmit = () => {
    if (numericQuantity <= 0) {
      alert('수량을 입력해주세요.');
      return;
    }

    if (orderPrice <= 0) {
      alert('주문 가격을 입력해주세요.');
      return;
    }

    sellOrder.sellStock({
      stockCode: stockCode,
      quantity: numericQuantity,
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
              onChange={(e) => setOrderPrice(Number(e.target.value))}
              className={styles.priceField}
            />
            <span className={styles.priceUnit}>원</span>
          </div>
          <div className={styles.priceButtons}>
            <button className={styles.priceBtn} onClick={() => handlePriceAdjust('down')}>
              -
            </button>
            <button className={styles.priceBtn} onClick={() => handlePriceAdjust('up')}>
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
                onChange={(e) => handleQuantityChange(e.target.value)}
                className={styles.quantityField}
                placeholder={`최대 ${formatNumber(currentHoldings)}주 가능`}
              />
              <span className={styles.quantityUnit}>주</span>
            </div>
            <div className={styles.quantityButtons}>
              <button
                className={styles.quantityAdjustBtn}
                onClick={() => handleQuantityChange(String(Math.max(1, numericQuantity - 1)))}
              >
                -
              </button>
              <button
                className={styles.quantityAdjustBtn}
                onClick={() => handleQuantityChange(String(numericQuantity + 1))}
              >
                +
              </button>
            </div>
          </div>
          <div className={styles.ratioButtons}>
            <button className={styles.ratioBtn} onClick={() => handleRatioClick(10)}>
              10%
            </button>
            <button className={styles.ratioBtn} onClick={() => handleRatioClick(25)}>
              25%
            </button>
            <button className={styles.ratioBtn} onClick={() => handleRatioClick(50)}>
              50%
            </button>
            <button className={styles.ratioBtn} onClick={() => handleRatioClick(100)}>
              최대
            </button>
          </div>
        </div>
      </div>

      {/* 정보 영역 */}
      <div className={styles.infoSection}>
        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>판매 후 잔여 수량</span>
          <span className={styles.infoValue}>{numericQuantity > 0 ? `${formatNumber(remainingQuantity)}주` : '-'}</span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>실현 손익</span>
          <span className={`${styles.infoValue} ${realizedProfitLoss >= 0 ? styles.positive : styles.negative}`}>
            {numericQuantity > 0
              ? `${realizedProfitLoss >= 0 ? '+' : ''}${formatNumber(Math.round(realizedProfitLoss))}원`
              : '-'}
          </span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>총 판매 금액</span>
          <span className={styles.infoValue}>{totalSellAmount > 0 ? `${formatNumber(totalSellAmount)}원` : '-'}</span>
        </div>
      </div>

      <button
        className={styles.submitButton}
        onClick={handleOrderSubmit}
        disabled={numericQuantity <= 0 || sellOrder.isLoading}
      >
        {sellOrder.isLoading ? '주문 중...' : '판매하기'}
      </button>
    </div>
  );
}
