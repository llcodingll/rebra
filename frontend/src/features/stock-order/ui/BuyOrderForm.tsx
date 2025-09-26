import { useState, useEffect, useCallback } from 'react';
import styles from './BuyOrderForm.module.css';
import { useBuyOrder } from '../hooks/useBuyOrder';
import type { StockHoldingData } from '../../stock-detail/api/types';

interface BuyOrderFormProps {
  stockCode: string;
  stockName: string;
  holdingData: StockHoldingData | null;
  currentPrice: number;
  orderBookClickedPrice?: number;
  onRefreshHolding?: () => void;
}

export default function BuyOrderForm({
  stockCode,
  stockName,
  holdingData,
  currentPrice,
  orderBookClickedPrice,
  onRefreshHolding,
}: BuyOrderFormProps) {
  // 내부 상태 관리 (기본값으로 초기화)
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

  // 현재 보유 정보 (holdingData에서 가져오기)
  const currentHoldings = holdingData?.holdingQuantity || 0;
  const currentAvgPrice = holdingData?.averagePurchasePrice || 0;
  const availableCash = holdingData?.availableCash || 0; // 실제 잔액 또는 기본값

  // 계산 값들
  const numericQuantity = typeof quantity === 'number' ? quantity : 0;
  const totalOrderAmount = orderPrice * numericQuantity;
  const maxAffordableQuantity = orderPrice > 0 ? Math.floor(availableCash / orderPrice) : 0;

  // 구매 후 예상 평균단가 계산
  const expectedAvgPrice =
    currentHoldings > 0 && numericQuantity > 0
      ? (currentHoldings * currentAvgPrice + totalOrderAmount) / (currentHoldings + numericQuantity)
      : numericQuantity > 0
      ? orderPrice
      : 0;

  // 가격 변경 검증 함수
  const handlePriceChange = (newPrice: number) => {
    const validPrice = Math.max(0, newPrice);

    // 구매 가격 자체가 구매가능금액을 초과하지 못하도록 제한
    if (validPrice > availableCash) {
      setOrderPrice(availableCash);
      alert('구매 가격이 구매 가능 금액을 초과하여 최대 금액으로 조정했습니다');
      return;
    }

    // 수량이 있을 때는 추가로 총액 체크
    if (numericQuantity > 0 && validPrice * numericQuantity > availableCash) {
      const maxAffordablePrice = Math.floor(availableCash / numericQuantity);
      setOrderPrice(maxAffordablePrice);
      alert('구매 가능 금액을 초과하여 최대 구매 가능 가격으로 조정했습니다');
      return;
    }

    setOrderPrice(validPrice);
  };

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

    // 총액 체크, 초과시 최대값으로 조정
    if (orderPrice > 0 && newQuantity > 0) {
      const maxAffordable = Math.floor(availableCash / orderPrice);
      if (newQuantity > maxAffordable) {
        setQuantity(maxAffordable);
        alert('구매 가능 금액을 초과하여 최대 구매 가능 수량으로 조정했습니다');
        return;
      }
    }

    setQuantity(newQuantity);
  };

  // 가격 조정 함수
  const handlePriceAdjust = (direction: 'up' | 'down') => {
    const step = 100;
    const newPrice = direction === 'up' ? orderPrice + step : Math.max(orderPrice - step, 0);
    handlePriceChange(newPrice);
  };

  // 비율 버튼 처리 (계좌 잔액 기준)
  const handleRatioClick = (ratio: number) => {
    const targetQuantity = Math.floor((maxAffordableQuantity * ratio) / 100);
    setQuantity(targetQuantity);
  };

  // 매수 주문 훅
  const buyOrder = useBuyOrder({
    stockCode,
    stockName,
    onSuccess: async (data) => {
      try {
        // 1. 먼저 보유 정보 갱신
        await onRefreshHolding?.();

        // 2. 갱신 완료 후 alert 표시
        alert(`매수 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);

        // 3. 폼 초기화
        setQuantity('');
      } catch (error) {
        // refetch 실패 시에도 alert 표시
        alert(`매수 주문이 완료되었습니다!\n주문번호: ${data.orderNumber}`);
        setQuantity('');
      }
    },
    onError: (error) => {
      alert(`매수 주문에 실패했습니다: ${error}`);
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

    buyOrder.buyStock({
      stockCode: stockCode,
      quantity: numericQuantity,
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
              onChange={(e) => handlePriceChange(Number(e.target.value))}
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
                placeholder={`최대 ${formatNumber(maxAffordableQuantity)}주 가능`}
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
          <span className={styles.infoLabel}>구매가능 금액</span>
          <span className={styles.infoValue}>{formatNumber(availableCash)}원</span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>구매 후 예상 평균</span>
          <span className={styles.infoValue}>
            {expectedAvgPrice > 0 ? `${formatNumber(Math.round(expectedAvgPrice))}원` : '-'}
          </span>
        </div>

        <div className={styles.infoRow}>
          <span className={styles.infoLabel}>총 구매 금액</span>
          <span className={styles.infoValue}>{totalOrderAmount > 0 ? `${formatNumber(totalOrderAmount)}원` : '-'}</span>
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
