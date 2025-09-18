import { motion } from 'motion/react';
import { Info } from 'lucide-react';
import styles from './MonthlyRebalancing.module.css';
import type { BacktestResultResponse } from '../../features/backtest/api/backtestApi';

interface MonthlyRebalancingProps {
  backtestResult?: BacktestResultResponse;
}

export default function MonthlyRebalancing({ backtestResult }: MonthlyRebalancingProps) {
  const calculateMonthlyRebalancing = () => {
    if (!backtestResult?.details || backtestResult.details.length === 0) {
      return [];
    }

    const monthlyCount: { [key: string]: number } = {};

    backtestResult.details.forEach(detail => {
      if (detail.is_rebalanced && detail.has_actual_trades) {
        const date = new Date(detail.period_date);
        const monthKey = `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`;
        monthlyCount[monthKey] = (monthlyCount[monthKey] || 0) + 1;
      }
    });

    const monthlyAggregate = Array(12).fill(0);

    Object.entries(monthlyCount).forEach(([monthKey, count]) => {
      const [year, month] = monthKey.split('-');
      const monthIndex = parseInt(month) - 1;
      monthlyAggregate[monthIndex] += count;
    });

    return monthlyAggregate;
  };

  const rebalancingData = calculateMonthlyRebalancing();

  const actualTotalRebalancing = backtestResult?.details?.filter(detail => detail.is_rebalanced && detail.has_actual_trades).length || 0;

  const calculateTotalMonths = () => {
    if (!backtestResult?.startDate || !backtestResult?.endDate) return 0;

    const startDate = new Date(backtestResult.startDate);
    const endDate = new Date(backtestResult.endDate);

    const yearDiff = endDate.getFullYear() - startDate.getFullYear();
    const monthDiff = endDate.getMonth() - startDate.getMonth();

    return yearDiff * 12 + monthDiff + 1;
  };

  const totalMonths = calculateTotalMonths();
  const averageRebalancing = totalMonths > 0
    ? (actualTotalRebalancing / totalMonths).toFixed(1)
    : '0';
  const maxValue = Math.max(...rebalancingData, 1);
  const chartHeight = 120; // 차트 높이 증가

  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.2 }}
      className={styles.rebalanceCard}
    >
      <h3 className={styles.rebalanceTitle}>월별 리밸런싱 횟수</h3>
      <div className={styles.barChart}>
        {rebalancingData.length > 0 ? rebalancingData.map((value, index) => {
          const displayHeight = maxValue > 0 ? (value / maxValue) * chartHeight : 0;

          const monthLabel = `${index + 1}월`;

          return (
            <div key={index} className={styles.barContainer}>
              <div className={styles.barValue}>{value}</div>
              {displayHeight > 0 && (
                <motion.div
                  className={styles.bar}
                  style={{
                    height: `${displayHeight}px`
                  }}
                  initial={{ height: 0 }}
                  animate={{ height: `${displayHeight}px` }}
                  transition={{ duration: 0.8, delay: 0.4 + index * 0.1 }}
                ></motion.div>
              )}
              <span className={styles.barLabel}>{monthLabel}</span>
            </div>
          );
        }) : (
          <div className={styles.emptyState}>
            <span>리밸런싱 데이터가 없습니다</span>
          </div>
        )}
      </div>
      <div className={styles.barChartFooter}>
        <Info className={styles.infoIcon} />
        <span>평균 {averageRebalancing}회/월</span>
      </div>
    </motion.div>
  );
}
