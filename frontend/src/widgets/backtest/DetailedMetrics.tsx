import { motion } from 'motion/react';
import { HelpCircle } from 'lucide-react';
import { useState } from 'react';
import styles from './DetailedMetrics.module.css';
import type { BacktestResultResponse } from '../features/backtest/api/backtestApi';

interface MetricData {
  label: string;
  value: string;
  isNegative?: boolean;
  isPositive?: boolean;
  description: string;
}

interface DetailedMetricsProps {
  backtestResult?: BacktestResultResponse;
}

export default function DetailedMetrics({ backtestResult }: DetailedMetricsProps) {
  const [hoveredMetric, setHoveredMetric] = useState<number | null>(null);

  // 실제 백테스트 결과에서 메트릭 데이터 생성
  const metrics: MetricData[] = backtestResult?.summary ? [
    {
      label: 'CAGR',
      value: `${backtestResult.summary.annualizedReturnPercentage?.toFixed(2) || 0}%`,
      isNegative: (backtestResult.summary.annualizedReturnPercentage || 0) < 0,
      isPositive: (backtestResult.summary.annualizedReturnPercentage || 0) > 0,
      description: '투자기간 동안의 연평균 수익률입니다. 복리효과를 고려한 실제 성장률을 나타냅니다.'
    },
    {
      label: '변동성',
      value: `${backtestResult.summary.volatilityPercentage?.toFixed(2) || 0}%`,
      description: '수익률이 얼마나 크게 오르내렸는지를 나타냅니다. 높을수록 위험하지만 큰 수익 가능성도 있습니다.'
    },
    {
      label: '최대 낙폭',
      value: `${backtestResult.summary.maxDrawdownPercentage?.toFixed(2) || 0}%`,
      isNegative: true,
      description: '투자기간 중 가장 큰 손실폭입니다. 최악의 상황에서 얼마나 손해를 볼 수 있는지 보여줍니다.'
    },
    {
      label: '샤프 비율',
      value: `${backtestResult.summary.sharpeRatio?.toFixed(3) || 0}`,
      isNegative: (backtestResult.summary.sharpeRatio || 0) < 0,
      isPositive: (backtestResult.summary.sharpeRatio || 0) > 0,
      description: '위험 대비 수익률을 나타냅니다. 1.0 이상이면 우수하며, 높을수록 효율적인 투자입니다.'
    },
    {
      label: '총 수익률',
      value: `${backtestResult.summary.totalReturnPercentage?.toFixed(2) || 0}%`,
      isNegative: (backtestResult.summary.totalReturnPercentage || 0) < 0,
      isPositive: (backtestResult.summary.totalReturnPercentage || 0) > 0,
      description: '투자기간 전체의 누적 수익률입니다. 초기 투자금 대비 얼마나 수익이 났는지 보여줍니다.'
    },
    {
      label: '리밸런싱 횟수',
      value: `${backtestResult.summary.rebalancingCount || 0}회`,
      description: '포트폴리오 비중을 다시 맞춘 횟수입니다. 너무 잦으면 거래비용이 증가할 수 있습니다.'
    },
    {
      label: '거래 비용',
      value: `-${(backtestResult.summary.totalFee || 0).toLocaleString()}원`,
      description: '주식 매매 시 발생한 수수료와 세금의 총합입니다. 수익률에서 차감되는 실제 비용입니다.'
    },
    {
      label: '대출 비용',
      value: `-${(backtestResult.summary.totalBorrowingCost || 0).toLocaleString()}원`,
      description: '주식 매수를 위해 일시적으로 발생한 대출의 이자비용입니다.'
    },
    {
      label: '초과 수익률',
      value: `${backtestResult.summary.excessReturnPercentage?.toFixed(2) || 0}%`,
      isNegative: (backtestResult.summary.excessReturnPercentage || 0) < 0,
      isPositive: (backtestResult.summary.excessReturnPercentage || 0) > 0,
      description: '단순 매수 후 보유(Buy & Hold) 대비 리밸런싱 전략의 추가 수익률입니다.'
    },
    {
      label: '최대 대출금액',
      value: `${(backtestResult.summary.maxBorrowingAmount || 0).toLocaleString()}원`,
      description: '리밸런싱 과정에서 발생한 가장 큰 대출금액입니다.'
    },
    {
      label: '최소 현금잔고',
      value: `${(backtestResult.summary.minCashBalance || 0).toLocaleString()}원`,
      description: '가장 적었던 현금 보유액입니다. 음수면 대출이 발생했음을 의미합니다.'
    }
  ] : [
    { label: 'CAGR', value: '데이터 없음', description: '투자기간 동안의 연평균 수익률입니다. 복리효과를 고려한 실제 성장률을 나타냅니다.' },
    { label: '변동성', value: '데이터 없음', description: '수익률이 얼마나 크게 오르내렸는지를 나타냅니다. 높을수록 위험하지만 큰 수익 가능성도 있습니다.' },
    { label: '최대 낙폭', value: '데이터 없음', description: '투자기간 중 가장 큰 손실폭입니다. 최악의 상황에서 얼마나 손해를 볼 수 있는지 보여줍니다.' },
    { label: '샤프 비율', value: '데이터 없음', description: '위험 대비 수익률을 나타냅니다. 1.0 이상이면 우수하며, 높을수록 효율적인 투자입니다.' },
    { label: '총 수익률', value: '데이터 없음', description: '투자기간 전체의 누적 수익률입니다. 초기 투자금 대비 얼마나 수익이 났는지 보여줍니다.' },
    { label: '리밸런싱 횟수', value: '데이터 없음', description: '포트폴리오 비중을 다시 맞춘 횟수입니다. 너무 잦으면 거래비용이 증가할 수 있습니다.' },
    { label: '거래 비용', value: '데이터 없음', description: '주식 매매 시 발생한 수수료와 세금의 총합입니다. 수익률에서 차감되는 실제 비용입니다.' },
    { label: '대출 비용', value: '데이터 없음', description: '주식 매수를 위해 일시적으로 발생한 대출의 이자비용입니다.' },
    { label: '초과 수익률', value: '데이터 없음', description: '단순 매수 후 보유(Buy & Hold) 대비 리밸런싱 전략의 추가 수익률입니다.' },
    { label: '최대 대출금액', value: '데이터 없음', description: '리밸런싱 과정에서 발생한 가장 큰 대출금액입니다.' },
    { label: '최소 현금잔고', value: '데이터 없음', description: '가장 적었던 현금 보유액입니다. 음수면 대출이 발생했음을 의미합니다.' }
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
            <div className={styles.labelContainer}>
              <span className={styles.detailLabel}>{metric.label}</span>
              <div 
                className={styles.helpIconContainer}
                onMouseEnter={() => setHoveredMetric(index)}
                onMouseLeave={() => setHoveredMetric(null)}
              >
                <HelpCircle className={styles.helpIcon} />
                {hoveredMetric === index && (
                  <div className={styles.tooltip}>
                    {metric.description}
                  </div>
                )}
              </div>
            </div>
            <span className={`${styles.detailValue} ${metric.isNegative ? styles.negative : metric.isPositive ? styles.positive : ''}`}>
              {metric.value}
            </span>
          </div>
        ))}
      </div>
    </motion.div>
  );
}