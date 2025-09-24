import styles from './HoldingInfoTable.module.css';
import type { StockHoldingData } from '../api/types';

interface HoldingInfoTableProps {
  holdingData: StockHoldingData | null;
  currentPrice?: number;
}

export default function HoldingInfoTable({ holdingData, currentPrice = 0 }: HoldingInfoTableProps) {
  const formatNumber = (value: number | undefined) => {
    if (value === undefined || value === null) return '-';
    return new Intl.NumberFormat('ko-KR').format(value);
  };

  const formatPercent = (value: number | undefined) => {
    if (value === undefined || value === null) return '-';
    const sign = value >= 0 ? '+' : '';
    return `${sign}${value.toFixed(2)}%`;
  };

  const getProfitStyle = (value: number | undefined) => {
    if (value === undefined || value === null) return styles.dataValue;
    if (value > 0) return `${styles.dataValue} ${styles.profitValue}`;
    if (value < 0) return `${styles.dataValue} ${styles.lossValue}`;
    return styles.dataValue;
  };

  // 보유하지 않는 경우
  if (!holdingData) {
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
            <div className={styles.columnHeader}>평균단가</div>
            <div className={styles.singleColumnData}>
              <div className={styles.singleDataValue}>
                {holdingData ? formatNumber(holdingData.averagePurchasePrice) : '-'}원
              </div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>평가손익</div>
            <div className={styles.columnHeaderSecond}>수익률</div>
            <div className={styles.columnData}>
              <div className={holdingData ? getProfitStyle(holdingData.evaluationProfitLoss) : styles.dataValue}>
                {holdingData ? formatNumber(holdingData.evaluationProfitLoss) : '-'}
              </div>
              <div className={holdingData ? getProfitStyle(holdingData.returnRate) : styles.dataValue}>
                {holdingData ? formatPercent(holdingData.returnRate) : '-'}
              </div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>매입금액</div>
            <div className={styles.columnHeaderSecond}>평가금액</div>
            <div className={styles.columnData}>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.purchaseAmount) : '-'}</div>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.evaluationAmount) : '-'}</div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>보유수량</div>
            <div className={styles.columnHeaderSecond}>가능수량</div>
            <div className={styles.columnData}>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.holdingQuantity) : '-'}</div>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.orderableQuantity) : '-'}</div>
            </div>
          </div>
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>수수료</div>
            <div className={styles.columnHeaderSecond}>세금</div>
            <div className={styles.columnData}>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.fee) : '-'}</div>
              <div className={styles.dataValue}>{holdingData ? formatNumber(holdingData.tax) : '-'}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
