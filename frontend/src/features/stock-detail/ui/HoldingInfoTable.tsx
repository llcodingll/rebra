import styles from './HoldingInfoTable.module.css';
import { useStockHolding } from '../hooks/useStockHolding';

interface HoldingInfoTableProps {
  stockCode: string;
  currentPrice?: number;
}

export default function HoldingInfoTable({ stockCode, currentPrice = 0 }: HoldingInfoTableProps) {
  const { holdingData, isLoading, hasAccount, hasHolding } = useStockHolding(stockCode, currentPrice);
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

  // 로딩 상태
  if (isLoading) {
    return (
      <div className={styles.holdingContainer}>
        <div className={styles.externalHeader}>현재 보유 정보</div>
        <div className={styles.holdingTable}>
          <div className={styles.emptyState}>보유 정보를 불러오는 중...</div>
        </div>
      </div>
    );
  }

  // 계정 연결 안됨
  if (!hasAccount) {
    return (
      <div className={styles.holdingContainer}>
        <div className={styles.externalHeader}>현재 보유 정보</div>
        <div className={styles.holdingTable}>
          <div className={styles.emptyState}>계정을 먼저 연결해주세요.</div>
        </div>
      </div>
    );
  }

  // 보유하지 않는 경우
  if (!hasHolding) {
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
              <div className={styles.dataValue}></div>
              <div className={styles.dataValue}></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
