import { useState, useRef, React } from 'react';
import { motion } from 'motion/react';
import styles from './BacktestResultsPage.module.css';
import ResultsHeader from '../../widgets/backtest/ResultsHeader';
import CumulativeReturnsChart from '../../widgets/backtest/CumulativeReturnsChart';
import MonthlyRebalancing from '../../widgets/backtest/MonthlyRebalancing';
import DetailedMetrics from '../../widgets/backtest/DetailedMetrics';

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

  // 샘플 거래 데이터
  const tradeData: TradeData[] = [
    { date: '2023-01', buyAmount: 1000000, sellAmount: 0, portfolioValue: 1000000 },
    { date: '2023-02', buyAmount: 500000, sellAmount: 200000, portfolioValue: 1020000 },
    { date: '2023-03', buyAmount: 300000, sellAmount: 100000, portfolioValue: 1050000 },
    { date: '2023-04', buyAmount: 200000, sellAmount: 300000, portfolioValue: 1090000 },
    { date: '2023-05', buyAmount: 400000, sellAmount: 150000, portfolioValue: 1150000 },
    { date: '2023-06', buyAmount: 100000, sellAmount: 250000, portfolioValue: 1180000 },
    { date: '2023-07', buyAmount: 600000, sellAmount: 100000, portfolioValue: 1250000 },
    { date: '2023-08', buyAmount: 200000, sellAmount: 400000, portfolioValue: 1290000 },
    { date: '2023-09', buyAmount: 300000, sellAmount: 200000, portfolioValue: 1320000 },
    { date: '2023-10', buyAmount: 150000, sellAmount: 350000, portfolioValue: 1280000 },
    { date: '2023-11', buyAmount: 400000, sellAmount: 100000, portfolioValue: 1300000 },
    { date: '2023-12', buyAmount: 250000, sellAmount: 300000, portfolioValue: 1350000 },
  ];

  // 성과 데이터 계산
  const portfolioValues = tradeData.map((d) => d.portfolioValue);
  const buyHoldValues = portfolioValues.map((_, i) => 1000000 * (1 + i * 0.024)); // 2.4% 월간 성장
  const kospiValues = portfolioValues.map((_, i) => 1000000 * (1 + i * 0.018)); // 1.8% 월간 성장

  // 퍼센트로 변환
  const portfolioPercents = portfolioValues.map((value) => (value / 1000000) * 100);
  const buyHoldPercents = buyHoldValues.map((value) => (value / 1000000) * 100);
  const kospiPercents = kospiValues.map((value) => (value / 1000000) * 100);

  // 차트 스케일 계산
  const allValues = [...portfolioPercents, ...buyHoldPercents, ...kospiPercents];
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const valueRange = maxValue - minValue;
  const padding = valueRange * 0.1;
  const chartMin = Math.max(minValue - padding, 95);
  const chartMax = maxValue + padding;
  const chartRange = chartMax - chartMin;

  // Y축 라벨 생성 (5개 구간)
  const yAxisLabels: string[] = [];
  for (let i = 0; i <= 4; i++) {
    const value = chartMin + (chartRange / 4) * i;
    yAxisLabels.unshift(value.toFixed(1) + '%');
  }

  // SVG 경로 생성
  const createPath = (values: number[]) => {
    const width = 800;
    const height = 400;
    const padding = 40;

    return values
      .map((value, index) => {
        const x = (index / (values.length - 1)) * (width - 2 * padding) + padding;
        const y = height - padding - ((value - chartMin) / chartRange) * (height - 2 * padding);
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  };

  return (
    <div className={styles.container}>
      <ResultsHeader />

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
            createPath={createPath}
          />
        </div>

        <div className={styles.sidebar}>
          <MonthlyRebalancing />
          <DetailedMetrics />
        </div>
      </div>
    </div>
  );
}
