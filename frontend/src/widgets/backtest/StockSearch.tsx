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
  startDate?: string;
  endDate?: string;
  isLoading?: boolean;
  searchError?: Error | null;
}

export default function StockSearch({
  searchTerm,
  setSearchTerm,
  filteredStocks,
  portfolioItems,
  onAddToPortfolio,
  startDate,
  endDate,
  isLoading,
  searchError
}: StockSearchProps) {
  const isSearchEnabled = startDate; // 시작일만 있으면 검색 가능
  return (
    <motion.div
      initial={{ opacity: 0, x: -20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.2 }}
      className={styles.stockSearchCard}
    >
      <div className={styles.cardHeader}>
        <h3>주식 검색</h3>
        {!isSearchEnabled && (
          <p className={styles.disabledMessage}>
            시작 날짜를 먼저 설정해주세요
          </p>
        )}
      </div>

      <div className={styles.searchWrapper}>
        <div className={styles.searchInputWrapper}>
          <Search className={styles.searchIcon} />
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            placeholder={isSearchEnabled ? "종목명 또는 종목코드를 입력하세요" : "시작 날짜를 먼저 설정해주세요"}
            className={`${styles.searchInput} ${!isSearchEnabled ? styles.disabled : ''}`}
            disabled={!isSearchEnabled}
          />
        </div>
      </div>

      <div className={`${styles.stockList} ${!isSearchEnabled ? styles.disabled : ''}`}>
        {!isSearchEnabled ? (
          <div className={styles.emptyState}>
            <p>시작 날짜를 설정하면 해당 시점의 주식 데이터를 검색할 수 있습니다.</p>
          </div>
        ) : searchError ? (
          <div className={styles.emptyState}>
            <p>{searchError.message}</p>
          </div>
        ) : !searchTerm.trim() ? (
          <div className={styles.emptyState}>
            <p>종목명을 입력해주세요.</p>
          </div>
        ) : isLoading ? (
          <div className={styles.emptyState}>
            <p>검색 중...</p>
          </div>
        ) : filteredStocks.length === 0 ? (
          <div className={styles.emptyState}>
            <p>검색 결과가 없습니다.</p>
          </div>
        ) : (
          filteredStocks.map((stock, index) => {
            const isAdded = portfolioItems.some(item => item.code === stock.code);
            return (
              <div
                key={stock.code}
                className={styles.stockItem}
              >
                <div className={styles.stockInfo}>
                  <div className={styles.stockHeader}>
                    <span className={styles.stockName}>{stock.name}</span>
                    <span className={styles.stockCode}>{stock.code}</span>
                  </div>
                  <div className={styles.stockPrice}>
                    <span className={styles.price}>{stock.price}</span>
                    <span className={`${styles.change} ${styles[stock.changeType]}`}>
                      {stock.change}
                    </span>
                  </div>
                  <div className={styles.stockDetails}>
                    <span>거래량: {stock.volume}</span>
                  </div>
                </div>

                <button
                  className={`${styles.addButton} ${isAdded ? styles.added : ''}`}
                  onClick={() => onAddToPortfolio(stock)}
                  disabled={isAdded}
                >
                  <Plus className={styles.addIcon} />
                  {isAdded ? '추가됨' : '추가'}
                </button>
              </div>
            );
          })
        )}
      </div>
    </motion.div>
  );
}