import styles from './StockDetailPage.module.css';
import imgChart from "figma:asset/7cbfeb4cacad8b7ff05087e6bb2f76c3ba4d921e.png";
import imgImage19 from "figma:asset/a35cbf8e782d1162ac0360ad37c72b41b56ed0c6.png";
import imgImage20 from "figma:asset/3a465bc3eceefca059097f79720cc85a3b28b734.png";

interface StockDetailPageProps {
  stockCode: string;
  onBack: () => void;
}

export default function StockDetailPage({ stockCode, onBack }: StockDetailPageProps) {
  // 예시 데이터 - 실제로는 API에서 받아올 데이터
  const stockData = {
    name: '삼성전자',
    code: '005930',
    price: 71400,
    change: 7000,
    changePercent: 12.2
  };

  return (
    <div className={styles.stockDetailPage}>
      {/* 뒤로가기 버튼 */}
      <button className={styles.backButton} onClick={onBack}>
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
          <path d="M19 12H5M12 19L5 12L12 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        </svg>
        목록으로 돌아가기
      </button>

      {/* 주식 기본 정보 */}
      <div className={styles.stockHeader}>
        <div className={styles.stockInfo}>
          <h1 className={styles.stockName}>{stockData.name}</h1>
          <span className={styles.stockCode}>{stockData.code}</span>
        </div>
        
        <div className={styles.priceInfo}>
          <div className={styles.currentPrice}>
            {stockData.price.toLocaleString()}원
          </div>
          <div className={styles.priceChange}>
            +{stockData.change.toLocaleString()}원({stockData.changePercent}%)
          </div>
        </div>
      </div>

      {/* 차트 및 정보 패널들 */}
      <div className={styles.chartContainer}>
        {/* 메인 차트 */}
        <div className={styles.mainChart}>
          <img src={imgChart} alt="주식 차트" className={styles.chartImage} />
        </div>

        {/* 호가창 */}
        <div className={styles.orderBook}>
          <img src={imgImage20} alt="호가창" className={styles.orderBookImage} />
        </div>

        {/* 추가 정보 패널 */}
        <div className={styles.infoPanel}>
          <img src={imgImage19} alt="추가 정보" className={styles.infoPanelImage} />
        </div>
      </div>
    </div>
  );
}