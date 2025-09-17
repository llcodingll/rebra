import React from 'react';
import { BarChart3 } from 'lucide-react';
import styles from './BacktestHistoryWidget.module.css';
import BacktestRow from '../../features/backtest/BacktestRow';
import type { BacktestListResponse } from '../../features/backtest/api/backtestApi';

interface BacktestHistoryWidgetProps {
  data?: BacktestListResponse[];
  onBacktestClick?: (backtest: BacktestListResponse) => void;
  onBacktestDelete?: (backtest: BacktestListResponse, index: number) => void;
  currentPage: number;
  itemsPerPage: number;
  isLoading?: boolean;
  error?: Error | null;
}

export default function BacktestHistoryWidget({
  data = [],
  onBacktestClick,
  onBacktestDelete,
  currentPage,
  itemsPerPage,
  isLoading,
  error
}: BacktestHistoryWidgetProps) {
  const paginatedData = data.slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage);

  // API 응답 데이터를 UI용 형태로 변환
  const transformBacktestData = (backtest: BacktestListResponse) => {
    const statusMap = {
      'PENDING': '대기 중',
      'PROCESSING': '계산 중',
      'COMPLETED': '완료',
      'FAILED': '실패'
    };

    const formatDate = (dateStr: string) => {
      return new Date(dateStr).toLocaleDateString('ko-KR');
    };

    const calculatePeriod = (startDate: string, endDate: string) => {
      const start = new Date(startDate);
      const end = new Date(endDate);
      const diffTime = Math.abs(end.getTime() - start.getTime());
      const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
      return `${diffDays}일`;
    };

    return {
      name: backtest.testName,
      date: formatDate(backtest.createdAt),
      period: calculatePeriod(backtest.startDate, backtest.endDate),
      backtestPeriod: `${formatDate(backtest.startDate)} ~ ${formatDate(backtest.endDate)}`,
      status: statusMap[backtest.status] || backtest.status
    };
  };

  return (
    <div className={styles.historySection}>

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
                <th>백테스트 기간</th>
                <th>상태</th>
                <th>관리</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                <tr>
                  <td colSpan={6} className={styles.emptyState}>
                    <div className={styles.emptyContent}>
                      <p className={styles.emptyTitle}>로딩 중...</p>
                    </div>
                  </td>
                </tr>
              ) : error ? (
                <tr>
                  <td colSpan={6} className={styles.emptyState}>
                    <div className={styles.emptyContent}>
                      <p className={styles.emptyTitle}>오류가 발생했습니다</p>
                      <p className={styles.emptySubtitle}>{error.message}</p>
                    </div>
                  </td>
                </tr>
              ) : paginatedData.length > 0 ? (
                paginatedData.map((backtest, index) => {
                  const transformedData = transformBacktestData(backtest);
                  return (
                    <BacktestRow
                      key={`${backtest.id}-${index}`}
                      name={transformedData.name}
                      date={transformedData.date}
                      period={transformedData.period}
                      backtestPeriod={transformedData.backtestPeriod}
                      status={transformedData.status}
                      onBacktestClick={backtest.status === 'COMPLETED' ? () => onBacktestClick?.(backtest) : undefined}
                      onDelete={() => onBacktestDelete?.(backtest, (currentPage - 1) * itemsPerPage + index)}
                    />
                  );
                })
              ) : (
                <tr>
                  <td colSpan={6} className={styles.emptyState}>
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