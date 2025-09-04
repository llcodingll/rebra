import styles from './ProfitStatusPage.module.css';
import imgPhoto14720996457855658Abf4Ff4E from "figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png";
import imgImage3 from "figma:asset/10ab81539875bd08fb11acc6c58753b6c244c1e0.png";
import imgImage22 from "figma:asset/1ea006dc4cf62fda2e8ddd425d0178712455c473.png";
import { imgSvg, imgSvg1, imgVector, imgVector1, imgVector2, imgVector3, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8, imgVector9, imgVector10, imgFrame, imgVector11, imgVector12, imgVector13, imgVector14, imgVector15 } from "../imports/svg-fecjf";

export default function ProfitStatusPage() {
  const stockData = [
    {
      name: '삼성전자',
      code: '005930',
      buyPrice: 68000,
      currentPrice: 71800,
      quantity: 50,
      totalValue: 3590000,
      profitRate: 5.6,
      profitAmount: 190000,
      currentWeight: 32.1,
      targetWeight: 30,
      weight: 6,
      thresholdWeight: 10,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'SK하이닉스',
      code: '000660',
      buyPrice: 85000,
      currentPrice: 89500,
      quantity: 30,
      totalValue: 2685000,
      profitRate: 5.3,
      profitAmount: 135000,
      currentWeight: 24.0,
      targetWeight: 25,
      weight: 5,
      thresholdWeight: 5,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'LG에너지솔루션',
      code: '373220',
      buyPrice: 390000,
      currentPrice: 412000,
      quantity: 15,
      totalValue: 6180000,
      profitRate: 5.6,
      profitAmount: 330000,
      currentWeight: 18.5,
      targetWeight: 20,
      weight: 4,
      thresholdWeight: 10,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: '삼성바이오로직스',
      code: '207940',
      buyPrice: 750000,
      currentPrice: 789000,
      quantity: 2,
      totalValue: 1578000,
      profitRate: 5.2,
      profitAmount: 78000,
      currentWeight: 14.1,
      targetWeight: 15,
      weight: 3,
      thresholdWeight: 5,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'NAVER',
      code: '035420',
      buyPrice: 175000,
      currentPrice: 183500,
      quantity: 25,
      totalValue: 4587500,
      profitRate: 4.9,
      profitAmount: 212500,
      currentWeight: 11.4,
      targetWeight: 10,
      weight: 2,
      thresholdWeight: 3,
      logo: imgPhoto14720996457855658Abf4Ff4E
    }
  ];

  const unregisteredStocks = [
    {
      name: '삼성전자',
      code: '005930',
      buyPrice: 68000,
      currentPrice: 71800,
      quantity: 50,
      totalValue: 3590000,
      profitRate: 5.6,
      profitAmount: 190000,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'SK하이닉스',
      code: '000660',
      buyPrice: 85000,
      currentPrice: 89500,
      quantity: 30,
      totalValue: 2685000,
      profitRate: 5.3,
      profitAmount: 135000,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'LG에너지솔루션',
      code: '373220',
      buyPrice: 390000,
      currentPrice: 412000,
      quantity: 15,
      totalValue: 6180000,
      profitRate: 5.6,
      profitAmount: 330000,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: '삼성바이오로직스',
      code: '207940',
      buyPrice: 750000,
      currentPrice: 789000,
      quantity: 2,
      totalValue: 1578000,
      profitRate: 5.2,
      profitAmount: 78000,
      logo: imgPhoto14720996457855658Abf4Ff4E
    },
    {
      name: 'NAVER',
      code: '035420',
      buyPrice: 175000,
      currentPrice: 183500,
      quantity: 25,
      totalValue: 4587500,
      profitRate: 4.9,
      profitAmount: 212500,
      logo: imgPhoto14720996457855658Abf4Ff4E
    }
  ];

  const rebalancingHistory = [
    {
      date: '2024-11-15',
      type: '자동',
      stockCount: 3,
      buyAmount: '+2,500,000원',
      sellAmount: '-1,800,000원',
      status: '성공'
    },
    {
      date: '2024-10-30', 
      type: '수동',
      stockCount: 5,
      buyAmount: '+3,200,000원',
      sellAmount: '-2,900,000원',
      status: '성공'
    },
    {
      date: '2024-10-15',
      type: '자동',
      stockCount: 2,
      buyAmount: '+1,200,000원',
      sellAmount: '-800,000원',
      status: '성공'
    }
  ];

  const chartLegendData = [
    { name: '삼성전자', weight: 32.1, color: '#3b82f6' },
    { name: 'SK하이닉스', weight: 24.0, color: '#10b981' },
    { name: 'LG에너지솔루션', weight: 18.5, color: '#f59e0b' },
    { name: '삼성바이오로직스', weight: 14.1, color: '#ef4444' },
    { name: 'NAVER', weight: 11.4, color: '#8b5cf6' }
  ];

  return (
    <div className={styles.profitStatusPage}>
      {/* 포트폴리오 헤더 */}
      <div className={styles.portfolioHeader}>
        <div className={styles.portfolioTitle}>
          <h2>A 포트폴리오</h2>
          <span className={styles.portfolioDesc}>은퇴 자금 마련</span>
        </div>
        <button className={styles.portfolioLink}>
          다른 포트폴리오 보기
        </button>
      </div>

      {/* 리밸런싱 컨트롤 */}
      <div className={styles.rebalancingControls}>
        <div className={styles.controlGroup}>
          <h3>즉시 실행</h3>
          <button className={styles.executeButton}>
            <img src={imgFrame} alt="" className={styles.playIcon} />
            지금 리벨런싱 실행
          </button>
        </div>

        <div className={styles.controlGroup}>
          <h3>자동 리벨런싱</h3>
          <div className={styles.toggleContainer}>
            <div className={styles.toggle}>
              <div className={styles.toggleTrack}></div>
              <div className={styles.toggleThumb}></div>
            </div>
            <span className={styles.toggleLabel}>활성화</span>
          </div>
        </div>

        <div className={styles.controlGroup}>
          <h3>리밸런싱 주기</h3>
          <div className={styles.periodButtons}>
            <button className={styles.periodButton}>주간</button>
            <button className={`${styles.periodButton} ${styles.active}`}>월간</button>
            <button className={styles.periodButton}>연간</button>
            <input type="number" className={styles.periodInput} defaultValue="3" />
            <span>개월마다</span>
            <button className={styles.saveButton}>저장</button>
          </div>
        </div>
      </div>

      {/* 포트폴리오 차트 및 정보 */}
      <div className={styles.portfolioSection}>
        <div className={styles.chartContainer}>
          <div className={styles.chartImageWrapper}>
            <img src={imgImage22} alt="포트폴리오 차트" className={styles.chartImage} />
          </div>

          <div className={styles.portfolioStats}>
            <div className={styles.totalReturn}>
              <h3>총 수익률</h3>
              <div className={styles.returnValue}>+3.2%</div>
              <div className={styles.returnAmount}>+405,000원</div>
            </div>

            <div className={styles.totalAssets}>
              <span>평가 자산: 180,620,000원</span>
            </div>
          </div>

          <div className={styles.chartLegend}>
            {chartLegendData.map((item, index) => (
              <div key={index} className={styles.legendItem}>
                <div className={styles.legendItemLeft}>
                  <div 
                    className={styles.legendColor} 
                    style={{ backgroundColor: item.color }}
                  ></div>
                  <span className={styles.legendName}>{item.name}</span>
                </div>
                <span className={styles.legendWeight}>{item.weight}%</span>
              </div>
            ))}
          </div>
        </div>

        <div className={styles.recentTrades}>
          <div className={styles.tradeItem}>
            <div className={styles.tradeHeader}>
              <span className={styles.stockName}>삼성전자</span>
              <div className={styles.tradeResult}>
                <span className={styles.profit}>손익 +180,000원</span>
                <span className={styles.profitRate}>+15.2%</span>
              </div>
            </div>
            <span className={styles.tradeDetail}>150주(1,200,000원) 매도</span>
          </div>

          <div className={styles.tradeItem}>
            <div className={styles.tradeHeader}>
              <span className={styles.stockName}>LG전자</span>
              <div className={styles.tradeResult}>
                <span className={styles.loss}>손익 -45,000원</span>
                <span className={styles.lossRate}>-5.3%</span>
              </div>
            </div>
            <span className={styles.tradeDetail}>80주(800,000원) 매수</span>
          </div>

          <div className={styles.tradeItem}>
            <div className={styles.tradeHeader}>
              <span className={styles.stockName}>채권 ETF</span>
              <div className={styles.tradeResult}>
                <span className={styles.profit}>손익 +25,000원</span>
                <span className={styles.profitRate}>+5.1%</span>
              </div>
            </div>
            <span className={styles.tradeDetail}>200주(500,000원) 매수</span>
          </div>
        </div>
      </div>

      {/* 테이블 섹션 */}
      <div className={styles.tablesSection}>
        {/* 등록 주식 테이블 */}
        <div className={styles.tableContainer}>
          <div className={styles.tableHeader}>
            <h3>등록 주식</h3>
          </div>
          
          <div className={styles.table}>
            <div className={styles.tableHead}>
              <div className={styles.columnHeader}>종목명</div>
              <div className={styles.columnHeader}>매수가/현재가</div>
              <div className={styles.columnHeader}>수량/평가금액</div>
              <div className={styles.columnHeader}>수익률</div>
              <div className={styles.columnHeader}>현재 비중(%)</div>
              <div className={styles.columnHeader}>목표 비중(%)</div>
              <div className={styles.columnHeader}>가중치</div>
              <div className={styles.columnHeader}>임계값 비중(%)</div>
              <div className={styles.columnHeader}>제외</div>
            </div>

            <div className={styles.tableBody}>
              {stockData.map((stock, index) => (
                <div key={index} className={styles.tableRow}>
                  <div className={styles.stockInfoCell}>
                    <span className={styles.stockName}>{stock.name}</span>
                    <span className={styles.stockCode}>{stock.code}</span>
                  </div>
                  
                  <div className={styles.priceCell}>
                    <div className={styles.buyPrice}>{stock.buyPrice.toLocaleString()}</div>
                    <div className={styles.currentPrice}>{stock.currentPrice.toLocaleString()}</div>
                  </div>
                  
                  <div className={styles.quantityCell}>
                    <div className={styles.quantity}>{stock.quantity}주</div>
                    <div className={styles.totalValue}>{stock.totalValue.toLocaleString()}원</div>
                  </div>
                  
                  <div className={styles.profitCell}>
                    <div className={styles.profitRate}>
                      <img src={imgVector11} alt="" className={styles.arrowIcon} />
                      +{stock.profitRate}%
                    </div>
                    <div className={styles.profitAmount}>(+{stock.profitAmount.toLocaleString()}원)</div>
                  </div>
                  
                  <div className={styles.weightCell}>
                    {stock.currentWeight}%
                  </div>
                  
                  <div className={styles.targetCell}>
                    <input 
                      type="number" 
                      defaultValue={stock.targetWeight} 
                      className={styles.targetInput}
                    />
                  </div>
                  
                  <div className={styles.weightValueCell}>
                    {stock.weight}
                  </div>
                  
                  <div className={styles.thresholdCell}>
                    <input 
                      type="number" 
                      defaultValue={stock.thresholdWeight}
                      className={styles.thresholdInput}
                    />
                  </div>
                  
                  <div className={styles.excludeCell}>
                    <img src={imgImage3} alt="제외" className={styles.excludeIcon} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 미등록 주식 테이블 */}
        <div className={styles.tableContainer}>
          <div className={styles.tableHeader}>
            <h3>미등록 주식</h3>
          </div>
          
          <div className={styles.table}>
            <div className={styles.tableHead}>
              <div className={styles.columnHeader}>종목명</div>
              <div className={styles.columnHeader}>매수가/현재가</div>
              <div className={styles.columnHeader}>수량/평가금액</div>
              <div className={styles.columnHeader}>수익률</div>
            </div>

            <div className={styles.tableBody}>
              {unregisteredStocks.map((stock, index) => (
                <div key={index} className={styles.tableRow}>
                  <div className={styles.stockInfoCell}>
                    <span className={styles.stockName}>{stock.name}</span>
                    <span className={styles.stockCode}>{stock.code}</span>
                  </div>
                  
                  <div className={styles.priceCell}>
                    <div className={styles.buyPrice}>{stock.buyPrice.toLocaleString()}</div>
                    <div className={styles.currentPrice}>{stock.currentPrice.toLocaleString()}</div>
                  </div>
                  
                  <div className={styles.quantityCell}>
                    <div className={styles.quantity}>{stock.quantity}주</div>
                    <div className={styles.totalValue}>{stock.totalValue.toLocaleString()}원</div>
                  </div>
                  
                  <div className={styles.profitCell}>
                    <div className={styles.profitRate}>
                      <img src={imgVector11} alt="" className={styles.arrowIcon} />
                      +{stock.profitRate}%
                    </div>
                    <div className={styles.profitAmount}>(+{stock.profitAmount.toLocaleString()}원)</div>
                  </div>
                  
                  <div className={styles.excludeCell}>
                    <img src={imgImage3} alt="제외" className={styles.excludeIcon} />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 리밸런싱 히스토리 */}
        <div className={styles.historyContainer}>
          <div className={styles.historyHeader}>
            <img src={imgVector13} alt="" className={styles.historyIcon} />
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
              {rebalancingHistory.map((item, index) => (
                <div key={index} className={styles.historyRow}>
                  <div className={styles.historyCell}>{item.date}</div>
                  <div className={styles.historyCell}>
                    <span className={`${styles.typeBadge} ${item.type === '자동' ? styles.auto : styles.manual}`}>
                      {item.type}
                    </span>
                  </div>
                  <div className={styles.historyCell}>{item.stockCount}개</div>
                  <div className={styles.historyCell}>
                    <span className={styles.buyAmount}>{item.buyAmount}</span>
                  </div>
                  <div className={styles.historyCell}>
                    <span className={styles.sellAmount}>{item.sellAmount}</span>
                  </div>
                  <div className={styles.historyCell}>
                    <span className={styles.statusBadge}>{item.status}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}