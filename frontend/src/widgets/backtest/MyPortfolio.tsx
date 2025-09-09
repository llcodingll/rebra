import { motion } from 'motion/react';
import { Trash2, TrendingUp } from 'lucide-react';
import styles from '../../pages/backtest/BacktestCreationPage.module.css';

interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: number;
  targetWeight: number;
  threshold: number;
}

interface MyPortfolioProps {
  portfolioItems: PortfolioItem[];
  setPortfolioItems: (items: PortfolioItem[]) => void;
  selectedPortfolio: string;
  totalValue: number;
  onRemoveFromPortfolio: (code: string) => void;
  parsePrice: (priceString: string) => number;
  formatPrice: (price: number) => string;
  calculateValue: (buyPrice: string, quantity: number) => number;
}

export default function MyPortfolio({
  portfolioItems,
  setPortfolioItems,
  selectedPortfolio,
  totalValue,
  onRemoveFromPortfolio,
  parsePrice,
  formatPrice,
  calculateValue
}: MyPortfolioProps) {
  return (
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.3 }}
      className={styles.portfolioCard}
    >
      <div className={styles.cardHeader}>
        <h3>나의 포트폴리오</h3>
        {selectedPortfolio && (
          <span className={styles.selectedPortfolio}>
            {selectedPortfolio}
          </span>
        )}
      </div>

      {portfolioItems.length > 0 ? (
        <>
          <div className={styles.portfolioTable}>
            <div className={styles.tableHeader}>
              <div className={styles.headerCell}>종목</div>
              <div className={styles.headerCell}>매수가</div>
              <div className={styles.headerCell}>수량</div>
              <div className={styles.headerCell}>가중치</div>
              <div className={styles.headerCell}>평가금액</div>
              <div className={styles.headerCell}>임계값</div>
              <div className={styles.headerCell}>삭제</div>
            </div>

            {portfolioItems.map((item, index) => (
              <motion.div
                key={item.code}
                initial={{ opacity: 0, y: 10 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ delay: index * 0.1 }}
                className={styles.tableRow}
              >
                <div className={styles.tableCell}>
                  <div className={styles.stockInfo}>
                    <span className={styles.stockName}>{item.name}</span>
                    <span className={styles.stockCode}>{item.code}</span>
                  </div>
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.priceText}>{item.buyPrice}</span>
                </div>
                <div className={styles.tableCell}>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => {
                      const newItems = [...portfolioItems];
                      newItems[index].quantity = parseInt(e.target.value) || 0;
                      setPortfolioItems(newItems);
                    }}
                    className={styles.numberInput}
                    min="1"
                  />
                  <span className={styles.unit}>주</span>
                </div>
                <div className={styles.tableCell}>
                  <input
                    type="number"
                    value={item.targetWeight}
                    onChange={(e) => {
                      const newItems = [...portfolioItems];
                      newItems[index].targetWeight = parseInt(e.target.value) || 0;
                      setPortfolioItems(newItems);
                    }}
                    className={styles.numberInput}
                    min="0"
                    max="100"
                  />
                  <span className={styles.unit}>%</span>
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.valueText}>
                    {formatPrice(calculateValue(item.buyPrice, item.quantity))}
                  </span>
                </div>
                <div className={styles.tableCell}>
                  <input
                    type="number"
                    value={item.threshold}
                    onChange={(e) => {
                      const newItems = [...portfolioItems];
                      newItems[index].threshold = parseInt(e.target.value) || 0;
                      setPortfolioItems(newItems);
                    }}
                    className={styles.numberInput}
                    min="0"
                    max="100"
                  />
                  <span className={styles.unit}>%</span>
                </div>
                <div className={styles.tableCell}>
                  <button
                    className={styles.deleteButton}
                    onClick={() => onRemoveFromPortfolio(item.code)}
                    title={`${item.name} 삭제`}
                  >
                    <Trash2 className={styles.deleteIcon} />
                  </button>
                </div>
              </motion.div>
            ))}
          </div>

          <div className={styles.portfolioSummary}>
            <div className={styles.summaryItem}>
              <TrendingUp className={styles.summaryIcon} />
              <span>총 평가금액:</span>
              <span className={styles.summaryValue}>
                {totalValue.toLocaleString()}원
              </span>
            </div>
            <div className={styles.summaryItem}>
              <span>총 종목 수:</span>
              <span className={styles.summaryValue}>
                {portfolioItems.length}개
              </span>
            </div>
          </div>
        </>
      ) : (
        <div className={styles.emptyState}>
          <TrendingUp className={styles.emptyIcon} />
          <h4>포트폴리오가 비어있습니다</h4>
          <p>왼쪽에서 종목을 검색하여 포트폴리오에 추가해보세요</p>
        </div>
      )}
    </motion.div>
  );
}