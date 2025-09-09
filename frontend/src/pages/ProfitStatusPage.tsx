import styles from './ProfitStatusPage.module.css';
import { profitStatusStockData, unregisteredStocksData, rebalancingHistoryData, chartLegendData } from '../mocks/profitStatusData';
import { imgSvg, imgSvg1, imgVector, imgVector1, imgVector2, imgVector3, imgVector4, imgVector5, imgVector6, imgVector7, imgVector8, imgVector9, imgVector10, imgFrame, imgVector11, imgVector12, imgVector13, imgVector14, imgVector15 } from "../assets/imports/svg-fecjf";

export default function ProfitStatusPage() {

  return (
    <div className={styles.profitStatusPage}>

      {/* 포트폴리오 차트 및 정보 */}
      <div className={styles.portfolioSection}>
        <div className={styles.chartContainer}>

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
          {/*비율별 포트폴리오 주식 목록*/}
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
          {/*비율별 포트폴리오 주식 목록*/}
        <div className={styles.recentTrades}>
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
              {rebalancingHistoryData.map((item, index) => (
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