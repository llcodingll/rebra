import styles from './AssetPortfolioChart.module.css';
// SVG paths are embedded directly in the component

export default function AssetPortfolioChart() {
  return (
    <div className={styles.chartContainer}>
      <div className={styles.chartCard}>

        {/* 차트 영역 */}
        <div className={styles.chartArea}>
          
          {/* 왼쪽 패널 (총 평가액 + 범례) */}
          <div className={styles.leftPanel}>
            {/* 총 평가액 정보 */}
            <div className={styles.totalValue}>
              <h3>총 평가액</h3>
              <div className={styles.amount}>18,620,500원</div>
              <div className={styles.change}>+405,000원(+3.2%)</div>
            </div>

            {/* 범례 */}
            <div className={styles.legend}>
              <div className={styles.legendItem}>
                <div className={`${styles.legendColor} ${styles.samsung}`}></div>
                <span className={styles.legendLabel}>삼성전자</span>
                <span className={styles.legendValue}>32.1%</span>
              </div>
              <div className={styles.legendItem}>
                <div className={`${styles.legendColor} ${styles.sk}`}></div>
                <span className={styles.legendLabel}>SK하이닉스</span>
                <span className={styles.legendValue}>24.0%</span>
              </div>
              <div className={styles.legendItem}>
                <div className={`${styles.legendColor} ${styles.lg}`}></div>
                <span className={styles.legendLabel}>LG에너지솔루션</span>
                <span className={styles.legendValue}>18.5%</span>
              </div>
              <div className={styles.legendItem}>
                <div className={`${styles.legendColor} ${styles.bio}`}></div>
                <span className={styles.legendLabel}>삼성바이오로직스</span>
                <span className={styles.legendValue}>14.1%</span>
              </div>
              <div className={styles.legendItem}>
                <div className={`${styles.legendColor} ${styles.naver}`}></div>
                <span className={styles.legendLabel}>NAVER</span>
                <span className={styles.legendValue}>11.4%</span>
              </div>
            </div>
          </div>

          {/* 가운데 차트 */}
          <div className={styles.chartWrapper}>
            <div className={styles.donutChart}>
              {/* SVG 도넛 차트 */}
            </div>
          </div>

          {/* 오른쪽 개별 주식 정보 */}
          <div className={styles.stockDetail}>
            <div className={styles.stockInfo}>
              <div className={styles.stockIcon}></div>
              <div className={styles.stockName}>
                <h4>삼성전자</h4>
                <span>005930</span>
              </div>
            </div>

            <div className={styles.stockMetrics}>
              <div className={styles.metric}>
                <label>수량</label>
                <span>50주</span>
              </div>
              <div className={styles.metric}>
                <label>평가금액</label>
                <span>3,590,000원</span>
              </div>
              <div className={styles.metric}>
                <label>수익률</label>
                <span className={styles.positive}>+8.5%</span>
              </div>
              <div className={styles.metric}>
                <label>현재 비중</label>
                <span>32.1%</span>
              </div>
            </div>

            <div className={styles.thresholdInfo}>
              <div className={styles.thresholdLabel}>임계값 비중</div>
              <div className={styles.thresholdBar}>
                <div className={styles.thresholdFill}></div>
              </div>
              <span className={styles.thresholdValue}>10%</span>
            </div>

            <div className={styles.targetInfo}>
              <div className={styles.targetLabel}>목표 비중</div>
              <div className={styles.targetBar}>
                <div className={styles.targetFill}></div>
              </div>
              <span className={styles.targetValue}>30%</span>
            </div>

            <div className={styles.compareTable}>
              <div className={styles.compareRow}>
                <span>현재</span>
                <span>목표</span>
                <span>필요량</span>
              </div>
              <div className={styles.compareValues}>
                <span>32.1%</span>
                <span className={styles.targetBlue}>30%</span>
                <span className={styles.negative}>-2.1%</span>
              </div>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
