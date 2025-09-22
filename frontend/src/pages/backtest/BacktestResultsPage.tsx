import { useState, useRef, useEffect, React } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'motion/react';
import styles from './BacktestResultsPage.module.css';
import ResultsHeader from '../../widgets/backtest/ResultsHeader';
import CumulativeReturnsChart from '../../widgets/backtest/CumulativeReturnsChart';
import MonthlyRebalancing from '../../widgets/backtest/MonthlyRebalancing';
import DetailedMetrics from '../../widgets/backtest/DetailedMetrics';
import PortfolioComposition from '../../widgets/backtest/PortfolioComposition';
import { getBacktestResult } from '../../features/backtest/api/backtestApi';

interface TradeData {
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
  cashBalance: number;
  dailyBorrowingInterest: number;
  buyHoldValue: number;
}

interface TooltipData {
  x: number;
  y: number;
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
}

export default function BacktestResultsPage() {
  const { id: backtestId } = useParams<{ id: string }>();

  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' });
  }, []);

  const { data: backtestResult, isLoading, error } = useQuery({
    queryKey: ['backtestResult', backtestId],
    queryFn: async () => {
      if (!backtestId) {
        throw new Error('백테스트 ID가 없습니다.');
      }

      const result = await getBacktestResult(parseInt(backtestId));

      if (result.success) {
        return result.data;
      } else {
        throw new Error(result.error.message);
      }
    },
    enabled: Boolean(backtestId),
    staleTime: 60000, // 결과 페이지는 정적이므로 60초 캐시
  });

  if (isLoading) {
    return null;
  }

  if (error) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <p>오류가 발생했습니다: {(error as Error).message}</p>
        </div>
      </div>
    );
  }


  if (!backtestResult) {
    return (
      <div className={styles.container}>
        <div className={styles.error}>
          <p>백테스트 결과를 찾을 수 없습니다.</p>
          <p>backtestId: {backtestId}</p>
          <p>isLoading: {String(isLoading)}</p>
          <p>error: {error ? String(error) : 'null'}</p>
          <p>backtestResult: {backtestResult ? 'exists' : 'null'}</p>
        </div>
      </div>
    );
  }

  const tradeData: TradeData[] = backtestResult.details?.map(detail => ({
    date: detail.period_date || '',
    buyAmount: detail.total_buy_amount || 0,
    sellAmount: detail.total_sell_amount || 0,
    portfolioValue: detail.portfolio_value || 0,
    cashBalance: detail.cash_balance || 0,
    dailyBorrowingInterest: detail.daily_borrowing_interest || 0,
    buyHoldValue: detail.buy_hold_value || 0,
  })) || [];

  const initialCapital = backtestResult?.summary?.finalValue && backtestResult?.summary?.totalReturn
    ? Math.round(backtestResult.summary.finalValue / (1 + backtestResult.summary.totalReturn))
    : 10000000;

  if (tradeData.length === 0 && backtestResult.summary) {
    const finalValue = backtestResult.summary.finalValue;
    const buyHoldFinalValue = initialCapital * (1 + (backtestResult.summary.buyHoldReturnPercentage || 0) / 100);
    tradeData.push(
      { date: backtestResult.startDate, buyAmount: initialCapital, sellAmount: 0, portfolioValue: initialCapital, cashBalance: 0, dailyBorrowingInterest: 0, buyHoldValue: initialCapital },
      { date: backtestResult.endDate, buyAmount: 0, sellAmount: 0, portfolioValue: finalValue, cashBalance: 0, dailyBorrowingInterest: 0, buyHoldValue: buyHoldFinalValue }
    );
  }

  const portfolioValues = tradeData.map((d) => d.portfolioValue);

  const buyHoldValues = tradeData.map((d) => d.buyHoldValue);

  const kospiReturnRate = 0.221;

  const kospiValues = portfolioValues.map((_, i, arr) => {
    const progress = arr.length > 1 ? i / (arr.length - 1) : 0;
    return initialCapital * (1 + kospiReturnRate * progress);
  });

  const portfolioPercents = portfolioValues.map((value) => (value / initialCapital) * 100);
  const buyHoldPercents = buyHoldValues.map((value) => (value / initialCapital) * 100);
  const kospiPercents = kospiValues.map((value) => (value / initialCapital) * 100);

  const rebalancingDates = backtestResult?.details
    ?.filter(detail => detail.is_rebalanced && detail.has_actual_trades)
    .map(detail => detail.period_date) || [];

  const allValues = [...portfolioPercents, ...buyHoldPercents];
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const valueRange = maxValue - minValue;
  const padding = valueRange * 0.1;
  const chartMin = Math.max(minValue - padding, 90);
  const chartMax = maxValue + padding;
  const chartRange = chartMax - chartMin;

  const yAxisLabels: string[] = [];
  for (let i = 0; i <= 4; i++) {
    const value = chartMin + (chartRange / 4) * i;
    yAxisLabels.unshift(value.toFixed(1) + '%');
  }


  return (
    <div className={styles.container}>
      <ResultsHeader backtestResult={backtestResult} />

      <div className={styles.content}>
        <div className={styles.mainChart}>
          <CumulativeReturnsChart
            tradeData={tradeData}
            portfolioPercents={portfolioPercents}
            buyHoldPercents={buyHoldPercents}
            kospiPercents={kospiPercents}
            chartMin={chartMin}
            chartMax={chartMax}
            chartRange={chartRange}
            yAxisLabels={yAxisLabels}
            portfolioFinalReturn={backtestResult?.summary?.totalReturnPercentage || 0}
            buyHoldFinalReturn={backtestResult?.summary?.buyHoldReturnPercentage || 0}
            kospiFinalReturn={22.1}
            rebalancingDates={rebalancingDates}
            showKospi={false}
          />
          <PortfolioComposition portfolioStocks={backtestResult?.portfolioStocks || []} />
        </div>

        <div className={styles.sidebar}>
          <MonthlyRebalancing backtestResult={backtestResult} />
          <DetailedMetrics backtestResult={backtestResult} />
        </div>
      </div>
    </div>
  );
}
