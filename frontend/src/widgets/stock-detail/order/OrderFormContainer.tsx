import { useState } from 'react';
import styles from './OrderFormContainer.module.css';
import OrderTabs, { OrderTab } from './OrderTabs';
import BuyOrderForm from '../../../features/stock-order/ui/BuyOrderForm';
import SellOrderForm from '../../../features/stock-order/ui/SellOrderForm';
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
  onRefreshHolding
}: OrderFormContainerProps) {
  const [activeTab, setActiveTab] = useState<OrderTab>('구매');

  const renderActiveForm = () => {
    const commonProps = {
      stockCode,
      stockName,
      holdingData,
      currentPrice,
      orderBookClickedPrice,
      onRefreshHolding
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
      <OrderTabs activeTab={activeTab} onTabChange={setActiveTab} />
      {renderActiveForm()}
    </div>
  );
}