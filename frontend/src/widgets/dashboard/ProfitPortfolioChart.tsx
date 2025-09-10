import { motion } from 'motion/react';
import styles from './ProfitPortfolioChart.module.css';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';

interface Stock {
  name: string;
  code: string;
  buyPrice: string;
  currentPrice: string;
  quantity: string;
  value: string;
  return: string;
  returnAmount: string;
  currentWeight: string;
  targetWeight: string;
  weight: string;
  threshold: string;
  type: 'registered' | 'unregistered';
}

interface ProfitPortfolioChartProps {
  data: Stock[];
}


export default function ProfitPortfolioChart({ data }: ProfitPortfolioChartProps) {
  // 총 평가액 계산
  const totalValue = data.reduce((sum, stock) => {
    const value = parseInt(stock.value.replace(/[^0-9]/g, ''));
    return sum + value;
  }, 0);

  // 총 수익 계산 
  const totalReturn = data.reduce((sum, stock) => {
    const returnAmount = parseInt(stock.returnAmount.replace(/[^0-9+-]/g, ''));
    return sum + returnAmount;
  }, 0);

  const totalReturnPercent = ((totalReturn / (totalValue - totalReturn)) * 100).toFixed(1);
  const isPositiveReturn = totalReturn >= 0;

  return (
    <div className={styles.chartContainer}>
      <motion.div 
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className={styles.chartCard}
      >
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerLeft}>
            <DollarSign className={styles.headerIcon} />
            <h2 className={styles.title}>수익률 분석</h2>
          </div>
          <div className={styles.headerBadge}>
            <span>총 {data.length}개 종목</span>
          </div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className={styles.content}>
          {/* 왼쪽 패널 */}
          <div className={styles.leftPanel}>
            {/* 총 수익률 정보 */}
            <motion.div 
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.totalValueSection}
            >
              <div className={styles.totalValueHeader}>
                <h3>총 수익률</h3>
                <div className={styles.returnIcon}>
                  {isPositiveReturn ? (
                    <TrendingUp className={styles.iconPositive} />
                  ) : (
                    <TrendingDown className={styles.iconNegative} />
                  )}
                </div>
              </div>
              <div className={styles.amount}>{isPositiveReturn ? '+' : ''}{totalReturnPercent}%</div>
              <div className={`${styles.returnInfo} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                <span className={styles.returnAmount}>
                  {totalReturn >= 0 ? '+' : ''}{totalReturn.toLocaleString()}원
                </span>
                <span className={styles.returnPercent}>
                  (총 평가액: {totalValue.toLocaleString()}원)
                </span>
              </div>
            </motion.div>

            {/* 리밸런싱 히스토리 */}
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
              className={styles.historyContainer}
            >
              <div className={styles.historyHeader}>
                <h3>리밸런싱 히스토리</h3>
              </div>
              
              <div className={styles.historyTable}>
                <div className={styles.historyTableHead}>
                  <div className={styles.historyColumnHeader}>실행 일시</div>
                  <div className={styles.historyColumnHeader}>유형</div>
                  <div className={styles.historyColumnHeader}>거래 종목</div>
                  <div className={styles.historyColumnHeader}>매수 금액</div>
                  <div className={styles.historyColumnHeader}>매도 금액</div>
                  <div className={styles.historyColumnHeader}>상태</div>
                </div>

                <div className={styles.historyTableBody}>
                  <div className={styles.historyRow}>
                    <div className={styles.historyCell}>2024-01-15</div>
                    <div className={styles.historyCell}>
                      <span className={`${styles.typeBadge} ${styles.auto}`}>
                        자동
                      </span>
                    </div>
                    <div className={styles.historyCell}>3개</div>
                    <div className={styles.historyCell}>
                      <span className={styles.buyAmount}>1,200,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.sellAmount}>800,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.statusBadge}>완료</span>
                    </div>
                  </div>
                  
                  <div className={styles.historyRow}>
                    <div className={styles.historyCell}>2024-01-10</div>
                    <div className={styles.historyCell}>
                      <span className={`${styles.typeBadge} ${styles.manual}`}>
                        수동
                      </span>
                    </div>
                    <div className={styles.historyCell}>2개</div>
                    <div className={styles.historyCell}>
                      <span className={styles.buyAmount}>900,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.sellAmount}>450,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.statusBadge}>완료</span>
                    </div>
                  </div>

                  <div className={styles.historyRow}>
                    <div className={styles.historyCell}>2024-01-05</div>
                    <div className={styles.historyCell}>
                      <span className={`${styles.typeBadge} ${styles.auto}`}>
                        자동
                      </span>
                    </div>
                    <div className={styles.historyCell}>4개</div>
                    <div className={styles.historyCell}>
                      <span className={styles.buyAmount}>1,500,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.sellAmount}>1,100,000원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.statusBadge}>진행중</span>
                    </div>
                  </div>
                </div>
              </div>
            </motion.div>
          </div>

          {/* 오른쪽: 차트 */}
          <motion.div 
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className={styles.chartWrapper}
          >
            
          </motion.div>
        </div>
      </motion.div>
    </div>
  );
}