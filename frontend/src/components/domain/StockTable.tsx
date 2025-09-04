import { ReactNode } from 'react';
import styles from './StockTable.module.css';

export interface StockData {
  rank?: number;
  name: string;
  code: string;
  price?: number;
  currentPrice?: number;
  buyPrice?: number;
  change?: number;
  changePercent?: boolean | number;
  volume?: string;
  quantity?: number;
  totalValue?: number;
  profitRate?: number;
  profitAmount?: number;
  currentWeight?: number;
  targetWeight?: number;
  weight?: number;
  thresholdWeight?: number;
  logo?: string;
  isFavorite?: boolean;
  sector?: string;
  category?: string;
  marketCap?: string;
}

interface StockTableProps {
  type: 'ranking' | 'search' | 'portfolio' | 'unregistered';
  title?: string;
  data: StockData[];
  onStockClick?: (stockCode: string) => void;
  onFavoriteToggle?: (stockCode: string) => void;
  onWeightChange?: (stockCode: string, field: string, value: number) => void;
  onExclude?: (stockCode: string) => void;
  showActions?: boolean;
  className?: string;
}

export default function StockTable({
  type,
  title,
  data,
  onStockClick,
  onFavoriteToggle,
  onWeightChange,
  onExclude,
  showActions = true,
  className = ""
}: StockTableProps) {
  const renderHeader = () => {
    switch (type) {
      case 'ranking':
        return (
          <>
            <div className={styles.headerCell}>종목</div>
            <div className={styles.headerCell}>현재가</div>
            <div className={styles.headerCell}>등락률</div>
            <div className={styles.headerCell}>거래대금</div>
          </>
        );
      case 'search':
        return (
          <>
            <div className={styles.headerCell}>종목정보</div>
            <div className={styles.headerCell}>가격정보</div>
            <div className={styles.headerCell}>차트</div>
          </>
        );
      case 'portfolio':
        return (
          <>
            <div className={styles.headerCell}>종목명</div>
            <div className={styles.headerCell}>매수가/현재가</div>
            <div className={styles.headerCell}>수량/평가금액</div>
            <div className={styles.headerCell}>수익률</div>
            <div className={styles.headerCell}>현재 비중(%)</div>
            <div className={styles.headerCell}>목표 비중(%)</div>
            <div className={styles.headerCell}>가중치</div>
            <div className={styles.headerCell}>임계값 비중(%)</div>
            {showActions && <div className={styles.headerCell}>제외</div>}
          </>
        );
      case 'unregistered':
        return (
          <>
            <div className={styles.headerCell}>종목명</div>
            <div className={styles.headerCell}>매수가/현재가</div>
            <div className={styles.headerCell}>수량/평가금액</div>
            <div className={styles.headerCell}>수익률</div>
            {showActions && <div className={styles.headerCell}>제외</div>}
          </>
        );
      default:
        return null;
    }
  };

  const renderRow = (stock: StockData, index: number) => {
    switch (type) {
      case 'ranking':
        return (
          <div
            key={stock.code}
            className={`${styles.row} ${index % 2 === 1 ? styles.evenRow : ''}`}
            onClick={() => onStockClick?.(stock.code)}
          >
            <div className={styles.stockInfo}>
              <div className={styles.favoriteIcon} onClick={(e) => {
                e.stopPropagation();
                onFavoriteToggle?.(stock.code);
              }}>
                <svg width="20" height="20" viewBox="0 0 20 20" fill={stock.isFavorite ? "#ef1515" : "#666"}>
                  <path d="M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z"/>
                </svg>
              </div>
              {stock.rank && <span className={styles.rank}>{stock.rank}</span>}
              {stock.logo && <img src={stock.logo} alt="" className={styles.logo} />}
              <span className={styles.stockName}>{stock.name}</span>
            </div>
            <div className={styles.price}>{stock.price?.toLocaleString()}</div>
            <div className={`${styles.change} ${stock.changePercent ? styles.positive : styles.negative}`}>
              {stock.changePercent ? '+' : ''}{stock.change}%
            </div>
            <div className={styles.volume}>{stock.volume}</div>
          </div>
        );
        
      case 'search':
        return (
          <div key={stock.code} className={styles.row}>
            <div className={styles.stockSearchInfo}>
              <div className={styles.stockMain}>
                <span className={styles.stockName}>{stock.name}</span>
                <span className={styles.stockCode}>{stock.code}</span>
                <span className={styles.stockSector}>{stock.sector}</span>
              </div>
              <div className={styles.stockCategory}>{stock.category}</div>
            </div>
            <div className={styles.stockPrice}>
              <div className={styles.priceMain}>
                <span className={styles.price}>{stock.price?.toLocaleString()}원</span>
                <span className={`${styles.change} ${typeof stock.changePercent === 'boolean' ? (stock.changePercent ? styles.positive : styles.negative) : ''}`}>
                  {stock.change}%
                </span>
              </div>
              <div className={styles.priceDetails}>
                <span>거래량: {stock.volume}</span>
                <span>시총: {stock.marketCap}</span>
              </div>
            </div>
            <div className={styles.stockChart}>
              <div className={styles.chartPlaceholder}></div>
            </div>
          </div>
        );
        
      case 'portfolio':
        return (
          <div key={stock.code} className={styles.row}>
            <div className={styles.stockInfoCell}>
              <span className={styles.stockName}>{stock.name}</span>
              <span className={styles.stockCode}>{stock.code}</span>
            </div>
            <div className={styles.priceCell}>
              <div className={styles.buyPrice}>{stock.buyPrice?.toLocaleString()}</div>
              <div className={styles.currentPrice}>{stock.currentPrice?.toLocaleString()}</div>
            </div>
            <div className={styles.quantityCell}>
              <div className={styles.quantity}>{stock.quantity}주</div>
              <div className={styles.totalValue}>{stock.totalValue?.toLocaleString()}원</div>
            </div>
            <div className={styles.profitCell}>
              <div className={styles.profitRate}>+{stock.profitRate}%</div>
              <div className={styles.profitAmount}>(+{stock.profitAmount?.toLocaleString()}원)</div>
            </div>
            <div className={styles.weightCell}>{stock.currentWeight}%</div>
            <div className={styles.targetCell}>
              <input 
                type="number" 
                defaultValue={stock.targetWeight} 
                className={styles.targetInput}
                onChange={(e) => onWeightChange?.(stock.code, 'targetWeight', parseInt(e.target.value))}
              />
            </div>
            <div className={styles.weightValueCell}>{stock.weight}</div>
            <div className={styles.thresholdCell}>
              <input 
                type="number" 
                defaultValue={stock.thresholdWeight}
                className={styles.thresholdInput}
                onChange={(e) => onWeightChange?.(stock.code, 'thresholdWeight', parseInt(e.target.value))}
              />
            </div>
            {showActions && (
              <div className={styles.excludeCell}>
                <button onClick={() => onExclude?.(stock.code)} className={styles.excludeButton}>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M5.33 4V2.67a.67.67 0 01.67-.67h2a.67.67 0 01.67.67V4m2 0v9.33a.67.67 0 01-.67.67H4a.67.67 0 01-.67-.67V4h8z" stroke="#999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
              </div>
            )}
          </div>
        );
        
      case 'unregistered':
        return (
          <div key={stock.code} className={styles.row}>
            <div className={styles.stockInfoCell}>
              <span className={styles.stockName}>{stock.name}</span>
              <span className={styles.stockCode}>{stock.code}</span>
            </div>
            <div className={styles.priceCell}>
              <div className={styles.buyPrice}>{stock.buyPrice?.toLocaleString()}</div>
              <div className={styles.currentPrice}>{stock.currentPrice?.toLocaleString()}</div>
            </div>
            <div className={styles.quantityCell}>
              <div className={styles.quantity}>{stock.quantity}주</div>
              <div className={styles.totalValue}>{stock.totalValue?.toLocaleString()}원</div>
            </div>
            <div className={styles.profitCell}>
              <div className={styles.profitRate}>+{stock.profitRate}%</div>
              <div className={styles.profitAmount}>(+{stock.profitAmount?.toLocaleString()}원)</div>
            </div>
            {showActions && (
              <div className={styles.excludeCell}>
                <button onClick={() => onExclude?.(stock.code)} className={styles.excludeButton}>
                  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
                    <path d="M2 4h12M5.33 4V2.67a.67.67 0 01.67-.67h2a.67.67 0 01.67.67V4m2 0v9.33a.67.67 0 01-.67.67H4a.67.67 0 01-.67-.67V4h8z" stroke="#999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
                </button>
              </div>
            )}
          </div>
        );
        
      default:
        return null;
    }
  };

  return (
    <div className={`${styles.stockTable} ${className}`}>
      {title && (
        <div className={styles.tableTitle}>
          <h3>{title}</h3>
        </div>
      )}
      
      <div className={styles.table}>
        <div className={styles.tableHeader}>
          {renderHeader()}
        </div>
        
        <div className={styles.divider}></div>
        
        <div className={styles.tableBody}>
          {data.map((stock, index) => renderRow(stock, index))}
        </div>
      </div>
    </div>
  );
}