import { motion } from 'motion/react';
import styles from './DetailedMetrics.module.css';

interface MetricData {
  label: string;
  value: string;
  isNegative?: boolean;
}

interface DetailedMetricsProps {
  metrics?: MetricData[];
}

export default function DetailedMetrics({
  metrics = [
    { label: 'CAGR', value: '35.2%' },
    { label: '변동성', value: '15.6%' },
    { label: '최대 낙폭', value: '-8.4%', isNegative: true },
    { label: '샤프 비율', value: '1.42' },
    { label: '승률', value: '72.2%' },
    { label: '총 거래횟수', value: '36회' },
    { label: '거래 비용', value: '-45,000원', isNegative: true }
  ]
}: DetailedMetricsProps) {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.6, delay: 0.6 }}
      className={styles.detailCard}
    >
      <h3 className={styles.detailTitle}>상세 측정</h3>
      <div className={styles.detailMetrics}>
        {metrics.map((metric, index) => (
          <div key={index} className={styles.detailMetric}>
            <span className={styles.detailLabel}>{metric.label}</span>
            <span className={`${styles.detailValue} ${metric.isNegative ? styles.negative : ''}`}>
              {metric.value}
            </span>
          </div>
        ))}
      </div>
    </motion.div>
  );
}