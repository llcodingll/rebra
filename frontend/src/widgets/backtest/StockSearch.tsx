import { motion } from 'motion/react';
import { Search, Plus } from 'lucide-react';
import styles from './StockSearch.module.css';

interface Stock {
  name: string;
  code: string;
  price: string;
  change: string;
  changeType: 'positive' | 'negative';
  volume: string;
  sector?: string;
}

interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: number;
  targetWeight: number;
  threshold: number;
}

interface StockSearchProps {
  searchTerm: string;
  setSearchTerm: (term: string) => void;
  filteredStocks: Stock[];
  portfolioItems: PortfolioItem[];
  onAddToPortfolio: (stock: Stock) => void;
}

export default function StockSearch({
  searchTerm,
  setSearchTerm,
  filteredStocks,
  portfolioItems,
  onAddToPortfolio
}: StockSearchProps) {
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
      className={styles.stockSearchCard}
    >
      <div className={styles.cardHeader}>
        <h3>주식 검색</h3>
      </div>

      <div className={styles.searchWrapper}>
        <div className={styles.searchInputWrapper}>
          <Search className={styles.searchIcon} />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder="종목명 또는 종목코드를 입력하세요"
            className={styles.searchInput}
          />
        </div>
      </div>

      <div className={styles.stockList}>
        {filteredStocks.map((stock, index) => (
          <motion.div
            key={stock.code}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.1 }}
            className={styles.stockItem}
          >
            <div className={styles.stockInfo}>
              <div className={styles.stockHeader}>
                <span className={styles.stockName}>{stock.name}</span>
                <span className={styles.stockCode}>{stock.code}</span>
              </div>
              <div className={styles.stockPrice}>
                <span className={styles.price}>{stock.price}</span>
              </div>
              <div className={styles.stockDetails}>
                <span>거래량: {stock.volume}</span>
              </div>
            </div>

            <button
              className={`${styles.addButton} ${
                portfolioItems.some(item => item.code === stock.code) ? styles.added : ''
              }`}
              onClick={() => onAddToPortfolio(stock)}
              disabled={portfolioItems.some(item => item.code === stock.code)}
            >
              <Plus className={styles.addIcon} />
              {portfolioItems.some(item => item.code === stock.code) ? '추가됨' : '추가'}
            </button>
          </motion.div>
        ))}
      </div>
    </motion.div>
  );
}