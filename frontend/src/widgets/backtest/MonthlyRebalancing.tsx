import { motion } from 'motion/react';
import { Info } from 'lucide-react';
import styles from './MonthlyRebalancing.module.css';
import type { BacktestResultResponse } from '../../features/backtest/api/backtestApi';

interface MonthlyRebalancingProps {
  backtestResult?: BacktestResultResponse;
}

export default function MonthlyRebalancing({ backtestResult }: MonthlyRebalancingProps) {
  // 실제 백테스트 데이터에서 리밸런싱 정보 추출
  // details가 없으면 기본 데이터 사용
  const rebalancingData = backtestResult?.details && backtestResult.details.length > 0
    ? backtestResult.details.map(() => 1) // details 기반으로 데이터 생성 (실제 구현 시 수정 필요)
    : [1, 2, 1, 3, 2, 1, 2, 1, 3, 2, 1, 2]; // 기본값

  const totalRebalancing = backtestResult?.summary?.rebalancingCount || 0;
  const averageRebalancing = rebalancingData.length > 0
    ? totalRebalancing / rebalancingData.length
    : 0;
  const maxValue = Math.max(...rebalancingData);
  const chartHeight = 100; // 차트 영역 높이 (px)

  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.2 }}
      className={styles.rebalanceCard}
    >
      <h3 className={styles.rebalanceTitle}>월별 리밸런싱 횟수</h3>
      <div className={styles.barChart}>
        {rebalancingData.map((value, index) => {
          const actualHeight = (value / maxValue) * chartHeight; // 실제 픽셀 높이

          return (
            <div key={index} className={styles.barContainer}>
              <div className={styles.barValue}>{value}</div>
              <motion.div
                className={styles.bar}
                style={{ height: `${actualHeight}px` }}
                initial={{ height: 0 }}
                animate={{ height: `${actualHeight}px` }}
                transition={{ duration: 0.8, delay: 0.4 + index * 0.1 }}
              ></motion.div>
              <span className={styles.barLabel}>{index + 1}월</span>
            </div>
          );
        })}
      </div>
      <div className={styles.barChartFooter}>
        <Info className={styles.infoIcon} />
        <span>평균 {averageRebalancing}회/월</span>
      </div>
    </motion.div>
  );
}
