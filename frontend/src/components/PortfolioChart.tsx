import styles from './PortfolioChart.module.css';
// SVG paths are embedded directly in the component

interface PortfolioChartProps {
  activeTab: 'assets' | 'profit';
  onTabChange: (tab: 'assets' | 'profit') => void;
}

export default function PortfolioChart({ activeTab, onTabChange }: PortfolioChartProps) {
  return (
    <div className={styles.chartContainer}>
      <div className={styles.chartCard}>
        {/* 탭 헤더 */}
        <div className={styles.tabHeader}>
          <button 
            className={`${styles.tab} ${activeTab === 'assets' ? styles.active : ''}`}
            onClick={() => onTabChange('assets')}
          >
            자산 현황
          </button>
          <button 
            className={`${styles.tab} ${activeTab === 'profit' ? styles.active : ''}`}
            onClick={() => onTabChange('profit')}
          >
            수익률 현황
          </button>
        </div>

        {/* 차트 영역 */}
        <div className={styles.chartArea}>
          <div className={styles.chartWrapper}>
            {/* SVG 도넛 차트 */}
            <div className={styles.donutChart}>
              <svg viewBox="0 0 308 238" fill="none" xmlns="http://www.w3.org/2000/svg">
                <g>
                  <path d="M306.465 236.559C306.465 199.584 297.779 163.129 281.11 130.142C264.441 97.1555 240.258 68.5643 210.516 46.6818C180.775 24.7994 146.31 10.2404 109.912 4.18279C73.5136 -1.87483 36.2033 0.739096 1 11.8131L36.2007 124.186C53.8024 118.649 72.4575 117.342 90.6567 120.371C108.856 123.4 126.088 130.679 140.959 141.62C155.83 152.562 167.921 166.857 176.256 183.351C184.59 199.844 188.933 218.072 188.933 236.559H306.465Z" fill="#3B82F6" stroke="white" strokeWidth="1.22396"/>
                  <path d="M146.383 1C95.091 22.2147 52.9416 60.9292 27.3946 110.292C1.84756 159.655 -5.44981 216.482 6.79397 270.718L121.429 244.73C115.307 217.612 118.956 189.198 131.729 164.517C144.503 139.836 165.577 120.478 191.224 109.871L146.383 1Z" fill="#10B981" stroke="white" strokeWidth="1.22396"/>
                  <path d="M1 36.9014C13.9834 77.5537 37.7452 113.907 69.7463 142.076C101.747 170.245 140.785 189.172 182.687 196.833L203.782 80.9656C182.831 77.1351 163.312 67.6719 147.312 53.5873C131.311 39.5027 119.43 21.3262 112.938 1L1 36.9014Z" fill="#F59E0B" stroke="white" strokeWidth="1.22396"/>
                  <path d="M0.999976 147.801C33.2396 150.821 65.7541 147.133 96.5048 136.969C127.255 126.805 155.579 110.384 179.699 88.7359L101.287 1.00004C89.2269 11.8242 75.0652 20.0347 59.6898 25.1166C44.3145 30.1986 28.0572 32.0425 11.9374 30.5327L0.999976 147.801Z" fill="#EF4444" stroke="white" strokeWidth="1.22396"/>
                  <path d="M86.7445 151.842C122.762 113.342 144.835 63.8371 149.424 11.2652L32.34 1.00003C30.0451 27.286 19.0086 52.0384 1.00002 71.2884L86.7445 151.842Z" fill="#8B5CF6" stroke="white" strokeWidth="1.22396"/>
                </g>
              </svg>
            </div>
          </div>

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

        {/* 개별 주식 정보 */}
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
  );
}