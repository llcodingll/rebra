import React from 'react';
import { BarChart3, Activity, TrendingUp } from 'lucide-react';
import styles from './BacktestHistoryWidget.module.css';
import BacktestRow from '../../features/backtest/BacktestRow';

interface BacktestData {
  name: string;
  date: string;
  period: string;
  totalReturn: string;
  maxDrawdown: string;
  sharpeRatio: string;
  annualReturn: string;
  volatility: string;
  winRate: string;
  status: string;
}

interface BacktestHistoryWidgetProps {
  data?: BacktestData[];
  onBacktestClick?: (backtest: BacktestData) => void;
  onBacktestDelete?: (backtest: BacktestData, index: number) => void;
  currentPage: number;
  itemsPerPage: number;
}

export default function BacktestHistoryWidget({ data = [], onBacktestClick, onBacktestDelete, currentPage, itemsPerPage }: BacktestHistoryWidgetProps) {
  const paginatedData = data.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  // 전체 통계 계산
  const totalBacktests = data.length;
  const completedBacktests = data.filter(item => item.status === '완료').length;
  const avgReturn = data.length > 0 
    ? (data.reduce((sum, item) => sum + parseFloat(item.totalReturn.replace('%', '')), 0) / data.length).toFixed(1)
    : '0.0';

  return (
    <div className={styles.historySection}>
      {/* 전체 통계 요약 */}
      <div className={styles.statsGrid}>
        <div className={styles.statCard}>
          <div className={styles.statContent}>
            <div className={styles.statText}>
              <p className={styles.statLabel}>총 백테스트</p>
              <p className={styles.statValue}>{totalBacktests}</p>
            </div>
            <BarChart3 className={styles.statIcon} />
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statContent}>
            <div className={styles.statText}>
              <p className={styles.statLabel}>완료된 백테스트</p>
              <p className={styles.statValue}>{completedBacktests}</p>
            </div>
            <Activity className={styles.statIcon} />
          </div>
        </div>
        <div className={styles.statCard}>
          <div className={styles.statContent}>
            <div className={styles.statText}>
              <p className={styles.statLabel}>평균 수익률</p>
              <p className={`${styles.statValue} ${parseFloat(avgReturn) >= 0 ? styles.positive : styles.negative}`}>
                {avgReturn}%
              </p>
            </div>
            <TrendingUp className={styles.statIcon} />
          </div>
        </div>
      </div>

      <div className={styles.historyCard}>
        <div className={styles.cardHeader}>
          <div className={styles.cardTitle}>
            <BarChart3 className={styles.titleIcon} />
            <h3>백테스트 성과 분석</h3>
          </div>
          <p className={styles.cardDescription}>
            포트폴리오 전략별 백테스트 결과 및 위험조정 수익률 분석
          </p>
        </div>
        
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.headerRow}>
                <th>전략명</th>
                <th>실행일자</th>
                <th>분석기간</th>
                <th>누적수익률</th>
                <th>최대낙폭 (MDD)</th>
                <th>샤프지수</th>
                <th>변동성</th>
                <th>승률</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {paginatedData.length > 0 ? (
                paginatedData.map((item, index) => (
                  <BacktestRow
                    key={`${item.name}-${index}`}
                    name={item.name}
                    date={item.date}
                    period={item.period}
                    totalReturn={item.totalReturn}
                    maxDrawdown={item.maxDrawdown}
                    sharpeRatio={item.sharpeRatio}
                    annualReturn={item.annualReturn}
                    volatility={item.volatility}
                    winRate={item.winRate}
                    status={item.status}
                    onBacktestClick={() => onBacktestClick?.(item)}
                    onDelete={() => onBacktestDelete?.(item, (currentPage - 1) * itemsPerPage + index)}
                  />
                ))
              ) : (
                <tr>
                  <td colSpan={10} className={styles.emptyState}>
                    <div className={styles.emptyContent}>
                      <BarChart3 className={styles.emptyIcon} />
                      <p className={styles.emptyTitle}>백테스트 데이터가 없습니다</p>
                      <p className={styles.emptySubtitle}>새로운 전략을 추가하여 백테스트를 실행해보세요</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}