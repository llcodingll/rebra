import styles from './OrderTabs.module.css';

export type OrderTab = '구매' | '판매';

interface OrderTabsProps {
  activeTab: OrderTab;
  onTabChange: (tab: OrderTab) => void;
}

export default function OrderTabs({ activeTab, onTabChange }: OrderTabsProps) {
  const tabs: OrderTab[] = ['구매', '판매'];

  return (
    <div className={styles.orderTabs}>
      {tabs.map((tab) => (
        <button
          key={tab}
          className={`${styles.orderTab} ${activeTab === tab ? styles.active : ''}`}
          onClick={() => onTabChange(tab)}
        >
          {tab}
        </button>
      ))}
    </div>
  );
}