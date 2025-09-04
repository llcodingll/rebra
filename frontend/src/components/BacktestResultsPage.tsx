import { useState } from 'react';
import styles from './BacktestResultsPage.module.css';

interface BacktestResultsPageProps {
  onBack: () => void;
  backtestData?: {
    name: string;
    date: string;
    period: string;
    totalReturn: string;
    maxDrawdown: string;
    sharpeRatio: string;
  };
}

export default function BacktestResultsPage({ onBack, backtestData }: BacktestResultsPageProps) {
  const defaultData = {
    name: '월간 리밸런싱 전략',
    date: '2024-01-15',
    period: '2023년 01월 01일 ~ 2023년 12월 31일',
    totalReturn: '+35.2%',
    maxDrawdown: '-8.4%',
    sharpeRatio: '1.42'
  };

  const data = backtestData || defaultData;

  return (
    <div className={styles.backtestResults}>
      <div className={styles.container}>
          {/* 백 버튼 */}
          <button className={styles.backButton} onClick={onBack}>
            ← 백테스트 목록으로 돌아가기
          </button>

          {/* 백테스트 결과 카드 */}
          <div className={styles.resultCard}>
            <h2 className={styles.resultTitle}>백테스트 결과</h2>
            
            <div className={styles.resultInfo}>
              <div className={styles.resultColumn}>
                <span className={styles.resultLabel}>백테스트 이름</span>
                <span className={styles.resultValue}>{data.name}</span>
                <span className={styles.resultLabel}>백테스트 기간</span>
                <span className={styles.resultValue}>{data.period}</span>
              </div>
              
              <div className={styles.resultColumn}>
                <span className={styles.resultLabel}>초기 자본</span>
                <span className={styles.resultValue}>10,000,000원</span>
                <span className={styles.resultLabel}>최종 자본</span>
                <span className={`${styles.resultValue} ${styles.blue}`}>13,520,000원</span>
              </div>
              
              <div className={styles.resultColumn}>
                <span className={styles.resultLabel}>리밸런싱 주기</span>
                <span className={styles.resultValue}>월간</span>
                <span className={styles.resultLabel}>총 수익률</span>
                <span className={`${styles.resultValue} ${styles.blue}`}>{data.totalReturn}</span>
              </div>
            </div>
          </div>

          {/* 지표 카드들 */}
          <div className={styles.metricsContainer}>
            <div className={styles.metricCard}>
              <span className={styles.metricLabel}>포트폴리오 수익률</span>
              <span className={`${styles.metricValue} ${styles.blue}`}>+35.2%</span>
              <span className={styles.metricSubtext}>연평균: +35.2%</span>
            </div>
            
            <div className={styles.metricCard}>
              <span className={styles.metricLabel}>Buy & Hold</span>
              <span className={styles.metricValue}>+28.9%</span>
              <span className={`${styles.metricSubtext} ${styles.green}`}>초과수익: +6.3%p</span>
            </div>
            
            <div className={styles.metricCard}>
              <span className={styles.metricLabel}>최대 낙폭</span>
              <span className={`${styles.metricValue} ${styles.red}`}>-8.4%</span>
              <span className={styles.metricSubtext}>2023년 4월</span>
            </div>
            
            <div className={styles.metricCard}>
              <span className={styles.metricLabel}>샤프 비율</span>
              <span className={styles.metricValue}>1.42</span>
              <span className={styles.metricSubtext}>변동성: 15.6%</span>
            </div>
          </div>

          {/* 차트와 상세 정보 */}
          <div className={styles.contentContainer}>
            {/* 누적 수익률 비교 차트 */}
            <div className={styles.chartCard}>
              <h3 className={styles.chartTitle}>누적 수익률 비교</h3>
              
              <div className={styles.chartContainer}>
                <div className={styles.chartArea}>
                  {/* Y축 라벨 */}
                  <div className={styles.yAxisLabels}>
                    <span>140</span>
                    <span>105</span>
                    <span>70</span>
                    <span>35</span>
                    <span>0</span>
                  </div>
                  
                  {/* 차트 그리드 */}
                  <div className={styles.chartGrid}>
                    <div className={styles.gridLines}>
                      {[0, 1, 2, 3, 4].map(i => (
                        <div key={i} className={styles.horizontalLine}></div>
                      ))}
                    </div>
                    
                    {/* 차트 라인 SVG */}
                    <svg className={styles.chartSvg} viewBox="0 0 900 456" preserveAspectRatio="none">
                      {/* 리밸런싱 전략 라인 */}
                      <path
                        d="M1 415.65C28.2331 412.285 55.4641 408.92 82.6972 405.228C109.93 401.537 137.163 393.5015 164.394 393.5015C191.628 393.5015 218.861 400.993 246.094 400.993C273.325 400.993 300.558 388.5083 327.791 382.4283C355.022 376.3483 382.255 369.2366 409.488 364.5132C436.721 359.7898 463.952 357.1849 491.185 354.0898C518.419 350.9966 545.652 345.9483 572.883 345.9483C600.116 345.9483 627.349 353.1132 654.58 353.1132C681.813 353.1132 709.046 337.7498 736.279 331.6166C763.51 325.4815 790.743 321.4098 817.977 316.3083C845.21 311.2049 872.441 306.1015 899.674 301"
                        stroke="#3B82F6"
                        strokeWidth="2"
                        fill="none"
                      />
                      
                      {/* Buy & Hold 라인 */}
                      <path
                        d="M1 395.1298C28.2331 392.8783 55.4641 390.6249 82.6972 388.2898C109.93 385.9566 137.163 381.1249 164.394 381.1249C191.628 381.1249 218.861 389.2683 246.094 389.2683C273.325 389.2683 300.558 374.4483 327.791 368.7483C355.022 363.0483 382.255 359.14 409.488 355.0683C436.721 350.9966 463.952 347.3049 491.185 344.32C518.419 341.3332 545.652 337.1532 572.883 337.1532C600.116 337.1532 627.349 345.6215 654.58 345.6215C681.813 345.6215 709.046 331.1283 736.279 325.4283C763.51 319.7283 790.743 315.4932 817.977 311.4215C845.21 307.3498 872.441 304.1749 899.674 301"
                        stroke="#10B981"
                        strokeWidth="2"
                        fill="none"
                      />
                      
                      {/* KOSPI 라인 */}
                      <path
                        d="M1 372.9815C28.2331 371.4083 55.4641 369.8332 82.6972 368.0966C109.93 366.36 137.163 362.56 164.394 362.56C191.628 362.56 218.861 373.6332 246.094 373.6332C273.325 373.6332 300.558 360.3332 327.791 355.3932C355.022 350.4532 382.255 347.1415 409.488 343.9932C436.721 340.8449 463.952 339 491.185 336.5015C518.419 334.0049 545.652 329.0117 572.883 329.0117C600.116 329.0117 627.349 337.8049 654.58 337.8049C681.813 337.8049 709.046 324.7766 736.279 319.8917C763.51 315.0049 790.743 311.64 817.977 308.4917C845.21 305.3415 872.441 303.1717 899.674 301"
                        stroke="#6B7280"
                        strokeWidth="2"
                        fill="none"
                      />
                      
                      {/* 데이터 포인트들 */}
                      {[82, 164, 246, 328, 410, 492, 573, 655, 737, 819, 900].map((x, i) => (
                        <g key={i}>
                          <circle cx={x} cy={415 - (i * 8)} r="6" fill="white" stroke="#3B82F6" strokeWidth="2"/>
                          <circle cx={x} cy={395 - (i * 6)} r="6" fill="white" stroke="#10B981" strokeWidth="2"/>
                          <circle cx={x} cy={375 - (i * 5)} r="6" fill="white" stroke="#6B7280" strokeWidth="2"/>
                        </g>
                      ))}
                    </svg>
                  </div>
                  
                  {/* X축 라벨 */}
                  <div className={styles.xAxisLabels}>
                    <span>2023-01</span>
                    <span>2023-03</span>
                    <span>2023-05</span>
                    <span>2023-07</span>
                    <span>2023-09</span>
                    <span>2023-12</span>
                  </div>
                </div>
              </div>
              
              <div className={styles.chartLegend}>
                <div className={styles.legendItem}>
                  <div className={`${styles.legendColor} ${styles.blue}`}></div>
                  <span>리밸런싱 전략</span>
                  <span className={`${styles.legendValue} ${styles.blue}`}>+35.2%</span>
                </div>
                <div className={styles.legendItem}>
                  <div className={`${styles.legendColor} ${styles.green}`}></div>
                  <span>Buy & Hold</span>
                  <span className={styles.legendValue}>+28.9%</span>
                </div>
                <div className={styles.legendItem}>
                  <div className={`${styles.legendColor} ${styles.gray}`}></div>
                  <span>KOSPI</span>
                  <span className={styles.legendValue}>+22.1%</span>
                </div>
              </div>
            </div>

            {/* 상세 통계 및 리밸런싱 횟수 */}
            <div className={styles.sideContainer}>
              {/* 상세 통계 */}
              <div className={styles.statsCard}>
                <h3 className={styles.statsTitle}>상세 통계</h3>
                
                <div className={styles.statsList}>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>CAGR</span>
                    <span className={styles.statValue}>35.2%</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>변동성</span>
                    <span className={styles.statValue}>15.6%</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>최대 낙폭</span>
                    <span className={`${styles.statValue} ${styles.red}`}>-8.4%</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>샤프 비율</span>
                    <span className={styles.statValue}>1.42</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>승률</span>
                    <span className={styles.statValue}>72.2%</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>총 거래횟수</span>
                    <span className={styles.statValue}>36회</span>
                  </div>
                  <div className={styles.statItem}>
                    <span className={styles.statLabel}>거래 비용</span>
                    <span className={`${styles.statValue} ${styles.red}`}>-45,000원</span>
                  </div>
                </div>
              </div>

              {/* 월별 리밸런싱 횟수 */}
              <div className={styles.rebalanceCard}>
                <h3 className={styles.rebalanceTitle}>월별 리밸런싱 횟수</h3>
                
                <div className={styles.barChart}>
                  <div className={styles.barChartContainer}>
                    {[4, 2, 6, 3, 1, 2, 3, 4, 5, 2, 1, 3].map((value, index) => (
                      <div key={index} className={styles.bar}>
                        <div 
                          className={styles.barFill}
                          style={{ height: `${(value / 6) * 100}%` }}
                        ></div>
                      </div>
                    ))}
                  </div>
                  
                  <div className={styles.barLabels}>
                    {['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'].map((month, index) => (
                      <span key={index} className={styles.barLabel}>{month}</span>
                    ))}
                  </div>
                </div>
                
                <p className={styles.rebalanceAverage}>평균: 3.0회/월</p>
              </div>
            </div>
          </div>
        </div>
    </div>
  );
}