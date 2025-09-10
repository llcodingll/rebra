import { useState } from 'react';
import { useParams } from 'react-router';
import RealTimeChart from '../../widgets/stock-detail/RealTimeChart';
import StockBasicInfo from '../../widgets/stock-detail/StockBasicInfo';
import HoldingInfoTable from '../../widgets/stock-detail/HoldingInfoTable';
import OrderBook from '../../widgets/stock-detail/OrderBook';
import OrderForm from '../../widgets/stock-detail/OrderForm';
import styles from './StockDetailPage.module.css';

interface OrderBookItem {
  price: number;
  quantity: number;
  size: number;
}

export default function StockDetailPage() {
  const { symbol } = useParams<{ symbol: string }>();
  const [quantity, setQuantity] = useState(0);
  const [selectedRatio, setSelectedRatio] = useState<number | null>(null);
  const [orderPrice, setOrderPrice] = useState(71400);

  // 실시간 데이터 상태
  const [realTimePrice, setRealTimePrice] = useState<number | null>(null);
  const [realTimePriceChange, setRealTimePriceChange] = useState<{ amount: number; rate: number } | null>(null);

  // 임시 주식 데이터
  const getStockInfo = (code: string) => {
    const stockData: Record<string, any> = {
      '005930': { name: '삼성전자', currentPrice: 71400 },
      '000660': { name: 'SK하이닉스', currentPrice: 125000 },
      '035420': { name: 'NAVER', currentPrice: 180000 },
      '051910': { name: 'LG화학', currentPrice: 420000 },
      '006400': { name: '삼성SDI', currentPrice: 250000 },
    };

    return stockData[code] || { name: '주식명', currentPrice: 50000 };
  };

  const stockCode = symbol || '005930';
  const stockData = getStockInfo(stockCode);

  const stockInfo = {
    code: stockCode,
    name: stockData.name,
    currentPrice: stockData.currentPrice,
    change: 7000,
    changePercent: 12.2,
    prevClose: stockData.currentPrice - 7000,
    volume: 54747,
    amount: 166530,
    high: stockData.currentPrice + 2700,
    low: stockData.currentPrice - 2600,
  };

  // 보유 현황 데이터
  const holdingData = {
    buyPrice: 54747,
    profitLoss: 166530,
    profitRate: 31.24,
    buyAmount: 547470,
    evaluationAmount: 714000,
    holdingQuantity: 10,
    availableQuantity: 10,
    fee: 1234,
    tax: 1234,
  };

  // 호가 데이터
  const orderBook = {
    asks: [
      { price: 72000, quantity: 119417, size: 1.34 },
      { price: 71800, quantity: 329778, size: 3.68 },
      { price: 71600, quantity: 244413, size: 2.73 },
      { price: 71400, quantity: 181658, size: 2.03 },
      { price: 71200, quantity: 187845, size: 2.1 },
    ],
    bids: [
      { price: 71000, quantity: 114635, size: 1.28 },
      { price: 70800, quantity: 19452, size: 0.22 },
      { price: 70600, quantity: 329778, size: 3.68 },
      { price: 70400, quantity: 244413, size: 2.73 },
      { price: 70200, quantity: 181658, size: 2.03 },
    ],
  };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  const handleOrderSubmit = () => {
    console.log('주문 제출:', { stockCode, quantity, orderPrice });
  };

  const handlePriceAdjust = (direction: 'up' | 'down') => {
    const step = 100;
    setOrderPrice((prev) => (direction === 'up' ? prev + step : Math.max(prev - step, 0)));
  };

  const handleRatioSelect = (ratio: number) => {
    setSelectedRatio(ratio);
    // 임시로 계산된 수량 (실제로는 보유 자금 기준으로 계산)
    const maxAffordable = Math.floor(1000000 / orderPrice);
    setQuantity(Math.floor(maxAffordable * (ratio / 100)));
  };

  // 테스트용 직접 접근 링크들
  if (window.location.pathname === '/stock-test') {
    return (
      <div style={{ padding: '20px', fontFamily: 'Arial, sans-serif' }}>
        <h2>주식 상세 페이지 테스트</h2>
        <p>아래 링크들을 클릭하여 다양한 종목의 상세 페이지를 확인해보세요:</p>
        <ul style={{ listStyle: 'none', padding: 0 }}>
          <li style={{ margin: '10px 0' }}>
            <a
              href='/dashboard/stocks/005930'
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px',
              }}
            >
              삼성전자 (005930)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a
              href='/dashboard/stocks/000660'
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px',
              }}
            >
              SK하이닉스 (000660)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a
              href='/dashboard/stocks/035420'
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px',
              }}
            >
              NAVER (035420)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a
              href='/dashboard/stocks/051910'
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px',
              }}
            >
              LG화학 (051910)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a
              href='/dashboard/stocks/006400'
              style={{
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px',
              }}
            >
              삼성SDI (006400)
            </a>
          </li>
        </ul>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      {/* 주식 정보 및 보유 현황 섹션 */}
      <div className={styles.stockInfoSection}>
        <div className={styles.stockBasicInfoWrapper}>
          <StockBasicInfo stockInfo={stockInfo} realTimePrice={realTimePrice} realTimePriceChange={realTimePriceChange} />
        </div>
        
        <div className={styles.holdingInfoWrapper}>
          <HoldingInfoTable holdingData={holdingData} />
        </div>
      </div>

      {/* 메인 콘텐츠 (3열 레이아웃) */}
      <div className={styles.mainContent}>
        {/* 좌측: 차트 */}
        <div className={styles.chartSection}>
          <RealTimeChart
            stockCode={stockInfo.code}
            stockName={stockInfo.name}
            onPriceUpdate={(price, change) => {
              setRealTimePrice(price);
              setRealTimePriceChange(change);
              setOrderPrice(price); // 주문 가격도 실시간으로 업데이트
            }}
          />
        </div>

        <OrderBook orderBook={orderBook} stockInfo={stockInfo} />

        <OrderForm
          orderPrice={orderPrice}
          onPriceAdjust={handlePriceAdjust}
          onQuantityChange={setQuantity}
          onRatioSelect={handleRatioSelect}
          onOrderSubmit={handleOrderSubmit}
          quantity={quantity}
          selectedRatio={selectedRatio}
        />
      </div>
    </div>
  );
}
