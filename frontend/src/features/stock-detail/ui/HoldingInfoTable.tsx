import styles from './HoldingInfoTable.module.css';
import { useStockDetail } from '../hooks/useStockDetail';

interface HoldingInfoTableProps {
  stockCode: string;
}

export default function HoldingInfoTable({ stockCode }: HoldingInfoTableProps) {
  const { data: holdingData, isLoading } = useStockDetail(stockCode);
  const formatNumber = (value: string | undefined) => {
    if (!value) return '-';
    const num = Number(value);
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPercent = (value: string | undefined) => {
    if (!value) return '-';
    const num = Number(value);
    const sign = num >= 0 ? '+' : '';
    return `${sign}${num}%`;
  };

  const getProfitStyle = (value: string | undefined) => {
    if (!value) return styles.dataValue;
    const num = Number(value);
    if (num > 0) return `${styles.dataValue} ${styles.profitValue}`;
    if (num < 0) return `${styles.dataValue} ${styles.lossValue}`;
    return styles.dataValue;
  };

  if (!holdingData && !isLoading) {
    return (
      <div className={styles.holdingContainer}>
        <div className={styles.externalHeader}>현재 보유 정보</div>
        <div className={styles.holdingTable}>
          <div className={styles.emptyState}>보유한 주식이 없습니다.</div>
        </div>
      </div>
    );
  }

  return (
    <div className={styles.holdingContainer}>
      <div className={styles.externalHeader}>현재 보유 정보</div>
      <div className={styles.holdingTable}>
        <div className={styles.tableContent}>
          <div className={`${styles.tableColumn} ${styles.singleColumn}`}>
            <div className={styles.columnHeader}>평균가</div>
            <div className={styles.singleColumnData}>
              <div className={styles.singleDataValue}>{holdingData ? formatNumber(holdingData.averagePrice) : '-'}원</div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>평가손익</div>
            <div className={styles.columnHeaderSecond}>수익률</div>
            <div className={styles.columnData}>
              <div className={holdingData ? getProfitStyle(holdingData.profitLoss) : styles.dataValue}>{holdingData ? formatNumber(holdingData.profitLoss) : '-'}</div>
              <div className={holdingData ? getProfitStyle(holdingData.profitLossRate) : styles.dataValue}>{holdingData ? formatPercent(holdingData.profitLossRate) : '-'}</div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>매입금액</div>
            <div className={styles.columnHeaderSecond}>평가금액</div>
            <div className={styles.columnData}>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.purchaseAmount) : '-'}</div>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.currentValue) : '-'}</div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>보유수량</div>
            <div className={styles.columnHeaderSecond}>가능수량</div>
            <div className={styles.columnData}>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.holdingQuantity) : '-'}</div>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.holdingQuantity) : '-'}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
