import { motion } from 'motion/react';
import { Info } from 'lucide-react';
import styles from '../../pages/backtest/BacktestResultsPage.module.css';

interface MonthlyRebalancingProps {
  rebalancingData?: number[];
  averageRebalancing?: number;
}

export default function MonthlyRebalancing({
  rebalancingData = [4, 2, 6, 3, 1, 2, 3, 4, 5, 2, 1, 3],
  averageRebalancing = 3.0,
}: MonthlyRebalancingProps) {
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
