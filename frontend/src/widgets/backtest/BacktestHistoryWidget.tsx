import React from 'react';
import styles from './BacktestHistoryWidget.module.css';
import BacktestRow from '../../features/backtest/BacktestRow';

interface BacktestData {
  name: string;
  date: string;
  period: string;
  totalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  status: string;
}

interface BacktestHistoryWidgetProps {
  data?: BacktestData[];
  onBacktestClick?: (backtest: BacktestData) => void;
  currentPage: number;
  itemsPerPage: number;
}

export default function BacktestHistoryWidget({ data = [], onBacktestClick, currentPage, itemsPerPage }: BacktestHistoryWidgetProps) {
  return (
    <div className={styles.historySection}>
      <div className={styles.historyCard}>
        <h3>백테스트 히스토리</h3>
        
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.headerRow}>
                <th>백테스트 이름</th>
                <th>생성일</th>
                <th>백테스트 기간</th>
                <th>총 수익률</th>
                <th>최대낙폭</th>
                <th>샤프비율</th>
                <th>상태</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {data
                .slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
                .map((item, index) => (
                  <BacktestRow
                    key={index}
                    name={item.name}
                    date={item.date}
                    period={item.period}
                    totalReturn={item.totalReturn}
                    maxDrawdown={item.maxDrawdown}
                    sharpeRatio={item.sharpeRatio}
                    status={item.status}
                    onBacktestClick={() => onBacktestClick?.(item)}
                  />
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}