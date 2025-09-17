import { motion } from 'motion/react';
import styles from './DetailedMetrics.module.css';
import type { BacktestResultResponse } from '../../features/backtest/api/backtestApi';

interface MetricData {
  label: string;
  value: string;
  isNegative?: boolean;
}

interface DetailedMetricsProps {
  backtestResult?: BacktestResultResponse;
}

export default function DetailedMetrics({ backtestResult }: DetailedMetricsProps) {
  // 실제 백테스트 결과에서 메트릭 데이터 생성
  const metrics: MetricData[] = backtestResult?.summary ? [
    {
      label: 'CAGR',
      value: `${backtestResult.summary.annualizedReturnPercentage?.toFixed(2) || 0}%`,
      isNegative: (backtestResult.summary.annualizedReturnPercentage || 0) < 0
    },
    {
      label: '변동성',
      value: `${backtestResult.summary.volatilityPercentage?.toFixed(2) || 0}%`
    },
    {
      label: '최대 낙폭',
      value: `${backtestResult.summary.maxDrawdownPercentage?.toFixed(2) || 0}%`,
      isNegative: true
    },
    {
      label: '샤프 비율',
      value: `${backtestResult.summary.sharpeRatio?.toFixed(3) || 0}`,
      isNegative: (backtestResult.summary.sharpeRatio || 0) < 0
    },
    {
      label: '총 수익률',
      value: `${backtestResult.summary.totalReturnPercentage?.toFixed(2) || 0}%`,
      isNegative: (backtestResult.summary.totalReturnPercentage || 0) < 0
    },
    {
      label: '리밸런싱 횟수',
      value: `${backtestResult.summary.rebalancingCount || 0}회`
    },
    {
      label: '거래 비용',
      value: `-${(backtestResult.summary.totalFee || 0).toLocaleString()}원`,
      isNegative: true
    }
  ] : [
    { label: 'CAGR', value: '데이터 없음' },
    { label: '변동성', value: '데이터 없음' },
    { label: '최대 낙폭', value: '데이터 없음' },
    { label: '샤프 비율', value: '데이터 없음' },
    { label: '총 수익률', value: '데이터 없음' },
    { label: '리밸런싱 횟수', value: '데이터 없음' },
    { label: '거래 비용', value: '데이터 없음' }
  ];
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