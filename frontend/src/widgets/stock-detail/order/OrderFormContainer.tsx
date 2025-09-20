import { useState } from 'react';
import styles from './OrderFormContainer.module.css';
import OrderTabs, { OrderTab } from './OrderTabs';
import BuyOrderForm from '../../../features/stock-order/ui/BuyOrderForm';
import SellOrderForm from '../../../features/stock-order/ui/SellOrderForm';

interface OrderFormContainerProps {
  stockCode: string;
  orderPrice: number;
  onPriceChange: (price: number) => void;
  onPriceAdjust: (direction: 'up' | 'down') => void;
  onQuantityChange: (quantity: number) => void;
  onRatioSelect: (ratio: number) => void;
  quantity: number;
  selectedRatio: number | null;
}

export default function OrderFormContainer({
  stockCode,
  orderPrice,
  onPriceChange,
  onPriceAdjust,
  onQuantityChange,
  onRatioSelect,
  quantity,
  selectedRatio
}: OrderFormContainerProps) {
  const [activeTab, setActiveTab] = useState<OrderTab>('구매');

  const renderActiveForm = () => {
    const commonProps = {
      stockCode,
      orderPrice,
      onPriceChange,
      onPriceAdjust,
      onQuantityChange,
      onRatioSelect,
      quantity,
      selectedRatio
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