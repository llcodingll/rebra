import { useState } from 'react';
import styles from './OrderFormContainer.module.css';
import OrderTabs, { OrderTab } from './OrderTabs';
import BuyOrderForm from '../../../features/stock-order/ui/BuyOrderForm';
import SellOrderForm from '../../../features/stock-order/ui/SellOrderForm';
import { isMarketOpen } from '../../../shared/util/marketTime';
import type { StockHoldingData } from '../../../features/stock-detail/api/types';

interface OrderFormContainerProps {
  stockCode: string;
  stockName: string;
  holdingData: StockHoldingData | null;
  currentPrice: number;
  orderBookClickedPrice?: number;
  onRefreshHolding?: () => void;
}

export default function OrderFormContainer({
  stockCode,
  stockName,
  holdingData,
  currentPrice,
  orderBookClickedPrice,
  onRefreshHolding,
}: OrderFormContainerProps) {
  const [activeTab, setActiveTab] = useState<OrderTab>('구매');
  const isMarketOpenNow = isMarketOpen();

  // 장시간이 아닐 때 표시할 메시지 컴포넌트
  const MarketClosedMessage = () => (
    <div className={styles.orderForm}>
      <div className={styles.emptyState}>
        <div className={styles.emptyMessage}>장시간이 아니에요</div>
        <div className={styles.emptySubMessage}>주식 거래는 평일 09:00 ~ 15:30에만 가능합니다.</div>
      </div>
    </div>
  );

  const renderActiveForm = () => {
    const commonProps = {
      stockCode,
      stockName,
      holdingData,
      currentPrice,
      orderBookClickedPrice,
      onRefreshHolding,
    };

    switch (activeTab) {
      case '구매':
        return <BuyOrderForm {...commonProps} />;
      case '판매':
        return <SellOrderForm {...commonProps} />;
      default:
        return <BuyOrderForm {...commonProps} />;
    }
  };

  return (
    <div className={styles.orderSection}>
      {isMarketOpenNow ? (
        <>
          <OrderTabs activeTab={activeTab} onTabChange={setActiveTab} />
          {renderActiveForm()}
        </>
      ) : (
        <MarketClosedMessage />
      )}
    </div>
  );
}
