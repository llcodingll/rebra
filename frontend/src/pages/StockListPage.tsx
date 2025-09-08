import { useState } from 'react';
import styles from './StockListPage.module.css';
import { stockListData, simpleNewsData } from '../mocks/stockListData';
import { imgIconHeroiconsMiniHttpsHeroiconsCom, imgIconHeroiconsMiniHttpsHeroiconsCom1, imgIconHeroiconsMiniHttpsHeroiconsCom2, imgIconHeroiconsMiniHttpsHeroiconsCom3, imgIconHeroiconsMiniHttpsHeroiconsCom4 } from '../assets/imports/svg-tn9np';

interface StockListPageProps {
  onStockSelect: (stockCode: string) => void;
}

export default function StockListPage({ onStockSelect }: StockListPageProps) {
  const [currentPage, setCurrentPage] = useState(2);


  return (
    <div className={styles.stockListPage}>
      {/* 메인 컨텐츠 영역 */}
      <div className={styles.mainContent}>
        {/* 주식 테이블 */}
        <div className={styles.stockTable}>
          {/* 테이블 헤더 */}
          <div className={styles.tableHeader}>
            <span className={styles.headerRank}>종목</span>
            <span className={styles.headerPrice}>현재가</span>
            <span className={styles.headerChange}>등락률</span>
            <span className={styles.headerVolume}>거래대금</span>
          </div>

          {/* 테이블 구분선 */}
          <div className={styles.divider}></div>

          {/* 테이블 바디 */}
          <div className={styles.tableBody}>
            {stockListData.map((stock, index) => (
              <div
                key={stock.rank}
                className={`${styles.stockRow} ${index % 2 === 1 ? styles.evenRow : ''}`}
                onClick={() => onStockSelect(stock.code)}
              >
                <div className={styles.stockInfo}>
                  <div className={styles.favoriteIcon}>
                    {stock.isFavorite ? (
                      <svg width="20" height="20" viewBox="0 0 20 20" fill="#ef1515">
                        <path d="M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z"/>
                      </svg>
                    ) : (
                      <svg width="20" height="20" viewBox="0 0 20 20" fill="#666">
                        <path d="M10 15.27L16.18 19l-1.64-7.03L20 7.24l-7.19-.61L10 0 7.19 6.63 0 7.24l5.46 4.73L3.82 19z"/>
                      </svg>
                    )}
                  </div>
                  <span className={styles.rank}>{stock.rank}</span>
                  <img src={stock.logo} alt="" className={styles.logo} />
                  <span className={styles.stockName}>{stock.name}</span>
                </div>
                
                <div className={styles.price}>
                  {stock.price.toLocaleString()}
                </div>
                
                <div className={`${styles.change} ${stock.changePercent ? styles.positive : styles.negative}`}>
                  {stock.changePercent ? '+' : ''}{stock.change}%
                </div>
                
                <div className={styles.volume}>
                  {stock.volume}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* 페이지네이션 */}
        <div className={styles.pagination}>
          <button className={styles.paginationButton}>
            <img src={imgIconHeroiconsMiniHttpsHeroiconsCom} alt="" />
            First
          </button>
          
          <button className={styles.paginationButton}>
            <img src={imgIconHeroiconsMiniHttpsHeroiconsCom1} alt="" />
            Back
          </button>

          <button className={styles.pageNumber}>1</button>
          <button className={`${styles.pageNumber} ${styles.active}`}>2</button>
          <button className={styles.pageNumber}>3</button>
          <button className={styles.pageNumber}>4</button>
          <button className={styles.pageNumber}>
            <img src={imgIconHeroiconsMiniHttpsHeroiconsCom2} alt="" />
          </button>
          <button className={styles.pageNumber}>25</button>

          <button className={styles.paginationButton}>
            Next
            <img src={imgIconHeroiconsMiniHttpsHeroiconsCom3} alt="" />
          </button>

          <button className={styles.paginationButton}>
            Last
            <img src={imgIconHeroiconsMiniHttpsHeroiconsCom4} alt="" />
          </button>
        </div>
      </div>

      {/* 뉴스 사이드바 */}
      <div className={styles.newsSidebar}>
        <div className={styles.newsHeader}>
          <h3>주요 뉴스</h3>
        </div>
        
        <div className={styles.newsList}>
          {simpleNewsData.map((news, index) => (
            <div key={index} className={styles.newsItem}>
              <div className={styles.newsContent}>
                <h4 className={styles.newsTitle}>{news.title}</h4>
                <span className={styles.newsTime}>{news.time}</span>
              </div>
              <div className={styles.newsImage}>
                <span>Image</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}