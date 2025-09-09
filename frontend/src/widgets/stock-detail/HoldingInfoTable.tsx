import styles from './HoldingInfoTable.module.css';

interface HoldingData {
  buyPrice: number;
  profitLoss: number;
  profitRate: number;
  buyAmount: number;
  evaluationAmount: number;
  holdingQuantity: number;
  availableQuantity: number;
  fee: number;
  tax: number;
}

interface HoldingInfoTableProps {
  holdingData: HoldingData;
}

export default function HoldingInfoTable({ holdingData }: HoldingInfoTableProps) {
  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  return (
    <div className={styles.holdingTable}>
      <div className={styles.tableHeader}>현재 보유 정보</div>
      <div className={styles.tableContent}>
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>매입가</div>
          <div className={styles.columnData}>
            <div className={styles.dataValue}>{formatNumber(holdingData.buyPrice)}원</div>
          </div>
        </div>
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>평가손익</div>
          <div className={styles.columnHeaderSecond}>수익률</div>
          <div className={styles.columnData}>
            <div className={styles.dataValue}>+{formatNumber(holdingData.profitLoss)}</div>
            <div className={styles.dataValue}>+{holdingData.profitRate}%</div>
          </div>
        </div>
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>매입금액</div>
          <div className={styles.columnHeaderSecond}>평가금액</div>
          <div className={styles.columnData}>
            <div className={styles.dataValue}>{formatNumber(holdingData.buyAmount)}</div>
            <div className={styles.dataValue}>{formatNumber(holdingData.evaluationAmount)}</div>
          </div>
        </div>
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>보유수량</div>
          <div className={styles.columnHeaderSecond}>가능수량</div>
          <div className={styles.columnData}>
            <div className={styles.dataValue}>{holdingData.holdingQuantity}</div>
            <div className={styles.dataValue}>{holdingData.availableQuantity}</div>
          </div>
        </div>
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>수수료</div>
          <div className={styles.columnHeaderSecond}>세금</div>
          <div className={styles.columnData}>
            <div className={styles.dataValue}>{holdingData.fee}</div>
            <div className={styles.dataValue}>{holdingData.tax}</div>
          </div>
        </div>
      </div>
    </div>
  );
}