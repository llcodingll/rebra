import { motion } from 'motion/react';
import { Calendar, BarChart3 } from 'lucide-react';
import styles from './ResultsHeader.module.css';
import type { BacktestResultResponse } from '../../features/backtest/api/backtestApi';

interface SummaryCardData {
  label: string;
  value: string;
  subtext?: string;
  highlight?: boolean;
}

interface ResultsHeaderProps {
  backtestResult?: BacktestResultResponse;
}

export default function ResultsHeader({
  backtestResult
}: ResultsHeaderProps) {
  const title = backtestResult?.testName || '백테스트 결과';

  const formatDate = (dateStr: string) => {
    return new Date(dateStr).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  };

  const period = backtestResult
    ? `${formatDate(backtestResult.startDate)} ~ ${formatDate(backtestResult.endDate)}`
    : '기간 정보 없음';

  const getRebalancingTypeLabel = (type: string) => {
    switch (type) {
      case 'THRESHOLD':
        return '임계값 기반';
      case 'PERIODIC':
        return '주기적 (월간)';
      default:
        return type;
    }
  };


  const initialCapital = backtestResult?.summary?.finalValue && backtestResult?.summary?.totalReturn
    ? Math.round(backtestResult.summary.finalValue / (1 + backtestResult.summary.totalReturn))
    : 0;

  const summaryCards: SummaryCardData[] = backtestResult?.summary ? [
    {
      label: '초기 자본',
      value: `${initialCapital.toLocaleString()}원`
    },
    {
      label: '최종 평가액',
      value: `${backtestResult.summary.finalValue.toLocaleString()}원`,
      highlight: true
    },
    {
      label: '최종 수익률',
      value: `${backtestResult.summary.totalReturnPercentage >= 0 ? '+' : ''}${backtestResult.summary.totalReturnPercentage.toFixed(2)}%`,
      highlight: true
    },
    {
      label: '총 리밸런싱 횟수',
      value: `${backtestResult.summary.rebalancingCount}회`
    }
  ] : [
    { label: '초기 자본', value: '데이터 없음' },
    { label: '최종 평가액', value: '데이터 없음' },
    { label: '최종 수익률', value: '데이터 없음' },
    { label: '총 리밸런싱 횟수', value: '데이터 없음' }
  ];

  return (
    <>
      <div className={styles.header}>
        <div className={styles.titleSection}>
          <div className={styles.titleContainer}>
            <h1 className={styles.title}>{title}</h1>
          </div>
          <div className={styles.headerMeta}>
            <div className={styles.periodInfo}>
              <Calendar className={styles.periodIcon} />
              <span className={styles.period}>{period}</span>
            </div>
            {backtestResult?.rebalancingType && (
              <div className={styles.rebalancingBadge}>
                <BarChart3 className={styles.rebalancingIcon} />
                <span>{getRebalancingTypeLabel(backtestResult.rebalancingType)}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 상단 요약 카드들 */}
      <div className={styles.summaryCards}>
        {summaryCards.map((card, index) => (
          <motion.div
            key={index}
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.6, delay: 0.1 * (index + 1) }}
            className={styles.summaryCard}
          >
            <div className={styles.summaryLabel}>{card.label}</div>
            <div className={`${styles.summaryValue} ${card.highlight ? styles.highlight : ''}`}>
              {card.value}
            </div>
            {card.subtext && (
              <div className={styles.summarySubtext}>{card.subtext}</div>
            )}
          </motion.div>
        ))}
      </div>
    </>
  );
}