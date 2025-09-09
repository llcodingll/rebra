import { useState } from 'react';
import { useParams } from 'react-router';
import RealTimeChart from '../widget/kis/RealTimeChart';
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
  const [activeTab, setActiveTab] = useState<'구매' | '판매' | '대기'>('구매');

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
    low: stockData.currentPrice - 2600
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
    tax: 1234
  };

  // 호가 데이터
  const orderBook = {
    asks: [
      { price: 72000, quantity: 119417, size: 1.34 },
      { price: 71800, quantity: 329778, size: 3.68 },
      { price: 71600, quantity: 244413, size: 2.73 },
      { price: 71400, quantity: 181658, size: 2.03 },
      { price: 71200, quantity: 187845, size: 2.10 },
    ],
    bids: [
      { price: 71000, quantity: 114635, size: 1.28 },
      { price: 70800, quantity: 19452, size: 0.22 },
      { price: 70600, quantity: 329778, size: 3.68 },
      { price: 70400, quantity: 244413, size: 2.73 },
      { price: 70200, quantity: 181658, size: 2.03 },
    ]
  };

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const formatPrice = (price: number) => {
    return `${formatNumber(price)}원`;
  };

  const handleOrderSubmit = () => {
    console.log('주문 제출:', { stockCode, quantity, orderPrice, activeTab });
  };

  const handlePriceAdjust = (direction: 'up' | 'down') => {
    const step = 100;
    setOrderPrice(prev => direction === 'up' ? prev + step : Math.max(prev - step, 0));
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
              href="/dashboard/stocks/005930"
              style={{ 
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px'
              }}
            >
              삼성전자 (005930)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a 
              href="/dashboard/stocks/000660"
              style={{ 
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px'
              }}
            >
              SK하이닉스 (000660)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a 
              href="/dashboard/stocks/035420"
              style={{ 
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px'
              }}
            >
              NAVER (035420)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a 
              href="/dashboard/stocks/051910"
              style={{ 
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px'
              }}
            >
              LG화학 (051910)
            </a>
          </li>
          <li style={{ margin: '10px 0' }}>
            <a 
              href="/dashboard/stocks/006400"
              style={{ 
                display: 'inline-block',
                padding: '10px 20px',
                backgroundColor: '#3b82f6',
                color: 'white',
                textDecoration: 'none',
                borderRadius: '8px',
                marginRight: '10px'
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
        {/* 좌측: 주식 기본 정보 */}
        <div className={styles.stockBasicInfo}>
          <div className={styles.stockTitle}>
            <h1 className={styles.stockName}>{stockInfo.name}</h1>
            <span className={styles.stockCode}>{stockInfo.code}</span>
          </div>
          <div className={styles.priceInfo}>
            <span className={styles.currentPrice}>{formatPrice(stockInfo.currentPrice)}</span>
            <div className={styles.priceChange}>
              <span className={styles.changeAmount}>+{formatNumber(stockInfo.change)}원</span>
              <span className={styles.changePercent}>(+{stockInfo.changePercent}%)</span>
            </div>
          </div>
        </div>

        {/* 우측: 현재 보유 정보 테이블 */}
        <div className={styles.holdingTable}>
          <div className={styles.tableHeader}>현재 보유 정보</div>
          <div className={styles.tableContent}>
            <div className={styles.tableColumn}>
              <div className={styles.columnHeader}>매입가</div>
              <div className={styles.columnData}>
                <div className={styles.dataValue}>{formatNumber(holdingData.buyPrice)}원</div>
              </div>
            </div>
            <div className={styles.tableColumn}>
              <div className={styles.columnHeader}>평가손익/수익률</div>
              <div className={styles.columnData}>
                <div className={styles.dataValue}>+{formatNumber(holdingData.profitLoss)}</div>
                <div className={styles.dataValue}>+{holdingData.profitRate}%</div>
              </div>
            </div>
            <div className={styles.tableColumn}>
              <div className={styles.columnHeader}>매입금액/평가금액</div>
              <div className={styles.columnData}>
                <div className={styles.dataValue}>{formatNumber(holdingData.buyAmount)}</div>
                <div className={styles.dataValue}>{formatNumber(holdingData.evaluationAmount)}</div>
              </div>
            </div>
            <div className={styles.tableColumn}>
              <div className={styles.columnHeader}>보유수량/가능수량</div>
              <div className={styles.columnData}>
                <div className={styles.dataValue}>{holdingData.holdingQuantity}</div>
                <div className={styles.dataValue}>{holdingData.availableQuantity}</div>
              </div>
            </div>
            <div className={styles.tableColumn}>
              <div className={styles.columnHeader}>수수료/세금</div>
              <div className={styles.columnData}>
                <div className={styles.dataValue}>{holdingData.fee}</div>
                <div className={styles.dataValue}>{holdingData.tax}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* 메인 콘텐츠 (3열 레이아웃) */}
      <div className={styles.mainContent}>
        {/* 좌측: 차트 */}
        <div className={styles.chartSection}>
          <RealTimeChart
            stockCode={stockInfo.code}
            stockName={stockInfo.name}
          />
        </div>

        {/* 가운데: 호가창 */}
        <div className={styles.orderBookSection}>
          <div className={styles.orderBookHeader}>
            <h3>호가</h3>
            <div className={styles.orderBookStats}>
              <span className={styles.bidTotal}>매수 총량: 134.99%</span>
              <span className={styles.askTotal}>매도 총량: 10.33%</span>
            </div>
          </div>
          
          <div className={styles.orderBook}>
            {/* 매도 호가 (상단) */}
            <div className={styles.asks}>
              {orderBook.asks.reverse().map((ask, index) => (
                <div key={index} className={styles.orderItem}>
                  <div className={styles.askBar} style={{ width: `${ask.size * 20}%` }}></div>
                  <span className={styles.quantity}>{formatNumber(ask.quantity)}</span>
                  <span className={styles.askPrice}>{formatNumber(ask.price)}</span>
                  <span className={styles.size}>{ask.size}%</span>
                </div>
              ))}
            </div>

            {/* 현재가 */}
            <div className={styles.currentPriceRow}>
              <span className={styles.currentPrice}>{formatNumber(stockInfo.currentPrice)}</span>
              <span className={styles.spread}>±{formatNumber(200)}</span>
            </div>

            {/* 매수 호가 (하단) */}
            <div className={styles.bids}>
              {orderBook.bids.map((bid, index) => (
                <div key={index} className={styles.orderItem}>
                  <div className={styles.bidBar} style={{ width: `${bid.size * 20}%` }}></div>
                  <span className={styles.quantity}>{formatNumber(bid.quantity)}</span>
                  <span className={styles.bidPrice}>{formatNumber(bid.price)}</span>
                  <span className={styles.size}>{bid.size}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 우측: 주문 입력창 */}
        <div className={styles.orderSection}>
          <div className={styles.orderTabs}>
            {(['구매', '판매', '대기'] as const).map((tab) => (
              <button
                key={tab}
                className={`${styles.orderTab} ${activeTab === tab ? styles.active : ''}`}
                onClick={() => setActiveTab(tab)}
              >
                {tab}
              </button>
            ))}
          </div>
          
          <div className={styles.orderForm}>
            <div className={styles.priceInput}>
              <label>주문 가격</label>
              <div className={styles.priceControls}>
                <input
                  type="text"
                  value={formatNumber(orderPrice)}
                  readOnly
                  className={styles.priceField}
                />
                <div className={styles.priceButtons}>
                  <button 
                    className={styles.priceBtn}
                    onClick={() => handlePriceAdjust('up')}
                  >
                    +
                  </button>
                  <button 
                    className={styles.priceBtn}
                    onClick={() => handlePriceAdjust('down')}
                  >
                    -
                  </button>
                </div>
              </div>
            </div>

            <div className={styles.quantitySection}>
              <label>수량</label>
              <div className={styles.quantityControls}>
                <input
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(Number(e.target.value))}
                  className={styles.quantityField}
                  placeholder="0"
                />
                <div className={styles.quantityButtons}>
                  <button 
                    className={`${styles.quantityBtn} ${selectedRatio === 10 ? styles.active : ''}`}
                    onClick={() => handleRatioSelect(10)}
                  >
                    10%
                  </button>
                  <button 
                    className={`${styles.quantityBtn} ${selectedRatio === 25 ? styles.active : ''}`}
                    onClick={() => handleRatioSelect(25)}
                  >
                    25%
                  </button>
                  <button 
                    className={`${styles.quantityBtn} ${selectedRatio === 50 ? styles.active : ''}`}
                    onClick={() => handleRatioSelect(50)}
                  >
                    50%
                  </button>
                  <button 
                    className={`${styles.quantityBtn} ${selectedRatio === 100 ? styles.active : ''}`}
                    onClick={() => handleRatioSelect(100)}
                  >
                    최대
                  </button>
                </div>
              </div>
              <div className={styles.quantityInfo}>
                <span>주문 금액: {formatNumber(orderPrice * quantity)}원</span>
              </div>
            </div>

            <button 
              className={styles.submitButton}
              onClick={handleOrderSubmit}
              disabled={quantity <= 0}
            >
              {activeTab}하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}