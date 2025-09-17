import { useState, useRef, useEffect, React } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { motion } from 'motion/react';
import styles from './BacktestResultsPage.module.css';
import ResultsHeader from '../../widgets/backtest/ResultsHeader';
import CumulativeReturnsChart from '../../widgets/backtest/CumulativeReturnsChart';
import MonthlyRebalancing from '../../widgets/backtest/MonthlyRebalancing';
import DetailedMetrics from '../../widgets/backtest/DetailedMetrics';
import { getBacktestResult } from '../../features/backtest/api/backtestApi';

interface TradeData {
  date: string;
  buyAmount: number;
  sellAmount: number;
  portfolioValue: number;
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

  // 백테스트 결과 조회
  const { data: backtestResult, isLoading, error } = useQuery({
    queryKey: ['backtestResult', backtestId],
    queryFn: async () => {
      console.log('🔍 백테스트 결과 조회 시작 - backtestId:', backtestId);

      if (!backtestId) {
        console.error('❌ 백테스트 ID가 없습니다');
        throw new Error('백테스트 ID가 없습니다.');
      }

      const result = await getBacktestResult(parseInt(backtestId));
      console.log('📊 백테스트 결과 API 응답:', result);

      if (result.success) {
        console.log('✅ 백테스트 결과 데이터:', result.data);
        return result.data;
      } else {
        console.error('❌ 백테스트 결과 API 에러:', result.error);
        throw new Error(result.error.message);
      }
    },
    enabled: Boolean(backtestId),
    staleTime: 60000, // 1분
  });

  // 로딩 중이거나 에러가 있는 경우
  if (isLoading) {
    return (
      <div className={styles.container}>
        <div className={styles.loading}>
          <p>백테스트 결과를 불러오는 중...</p>
        </div>
      </div>
    );
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

  console.log('🎯 현재 상태:', { backtestId, isLoading, error, backtestResult });

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

  // 실제 백테스트 결과에서 차트 데이터 생성
  const tradeData: TradeData[] = backtestResult.details?.map(detail => ({
    date: detail.date || '',
    buyAmount: detail.buyAmount || 0,
    sellAmount: detail.sellAmount || 0,
    portfolioValue: detail.portfolioValue || 0,
  })) || [];

  // 초기 자본 계산 (먼저 계산)
  const initialCapital = backtestResult?.summary?.finalValue && backtestResult?.summary?.totalReturn
    ? Math.round(backtestResult.summary.finalValue / (1 + backtestResult.summary.totalReturn))
    : 10000000; // 기본값 1천만원

  // details가 없으면 기본 데이터 생성 (시작일과 종료일 기준)
  if (tradeData.length === 0 && backtestResult.summary) {
    const finalValue = backtestResult.summary.finalValue;
    tradeData.push(
      { date: backtestResult.startDate, buyAmount: initialCapital, sellAmount: 0, portfolioValue: initialCapital },
      { date: backtestResult.endDate, buyAmount: 0, sellAmount: 0, portfolioValue: finalValue }
    );
  }

  // 성과 데이터 계산
  const portfolioValues = tradeData.map((d) => d.portfolioValue);

  // 실제 백테스트 수익률 기반으로 Buy & Hold 데이터 계산
  const totalReturnRate = (backtestResult?.summary?.totalReturnPercentage || 0) / 100;
  const buyHoldReturnRate = (backtestResult?.summary?.buyHoldReturnPercentage || 0) / 100;
  const kospiReturnRate = 0.221; // KOSPI 22.1% 임시값

  // 기간에 따른 선형 증가로 계산 (단순화)
  const buyHoldValues = portfolioValues.map((_, i, arr) => {
    const progress = arr.length > 1 ? i / (arr.length - 1) : 0;
    return initialCapital * (1 + buyHoldReturnRate * progress);
  });

  const kospiValues = portfolioValues.map((_, i, arr) => {
    const progress = arr.length > 1 ? i / (arr.length - 1) : 0;
    return initialCapital * (1 + kospiReturnRate * progress);
  });

  // 퍼센트로 변환 (초기 자본 기준)
  const portfolioPercents = portfolioValues.map((value) => (value / initialCapital) * 100);
  const buyHoldPercents = buyHoldValues.map((value) => (value / initialCapital) * 100);
  const kospiPercents = kospiValues.map((value) => (value / initialCapital) * 100);

  // 차트 스케일 계산
  const allValues = [...portfolioPercents, ...buyHoldPercents, ...kospiPercents];
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const valueRange = maxValue - minValue;
  const padding = valueRange * 0.1;
  const chartMin = Math.max(minValue - padding, 90);
  const chartMax = maxValue + padding;
  const chartRange = chartMax - chartMin;

  // Y축 라벨 생성 (5개 구간)
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
          />
        </div>

        <div className={styles.sidebar}>
          <MonthlyRebalancing backtestResult={backtestResult} />
          <DetailedMetrics backtestResult={backtestResult} />
        </div>
      </div>
    </div>
  );
}
