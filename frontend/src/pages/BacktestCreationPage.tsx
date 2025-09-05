import { useState } from 'react';
import styles from './BacktestCreationPage.module.css';
import Button from '../components/common/Button';
import Input from '../components/common/Input';
import Card from '../components/common/Card';
import { stockData } from '../mocks/stocks';
import { portfolioData } from '../mocks/portfolio';
import {
  imgFrame, imgFrame1, imgFrame2, imgFrame3, imgSvg
} from '../imports/svg-l1em5';

interface BacktestCreationPageProps {
  onBack: () => void;
  selectedPortfolio?: string;
}

export default function BacktestCreationPage({ onBack, selectedPortfolio }: BacktestCreationPageProps) {
  const [backtestName, setBacktestName] = useState('');
  const [rebalancingPeriod, setRebalancingPeriod] = useState('반기');
  const [startDate, setStartDate] = useState('2023년 01월');
  const [endDate, setEndDate] = useState('2023년 12월');

  const handleRunBacktest = () => {
    console.log('백테스트 실행:', {
      name: backtestName,
      period: rebalancingPeriod,
      startDate,
      endDate
    });
  };


  return (
    <div className={styles.backtestCreation}>
      {/* 헤더 */}
      <div className={styles.header}>
        <Button variant="ghost" onClick={onBack}>
          ← 뒤로 가기
        </Button>
        <h1>백테스트 생성</h1>
      </div>

      <div className={styles.container}>
        {/* 백테스트 설정 카드 */}
        <div className={styles.settingsCard}>
          <div className={styles.cardHeader}>
            <h3>백테스트 설정</h3>
          </div>

          <div className={styles.formGrid}>
            <div className={styles.formGroup}>
              <label>테스트 이름</label>
              <Input
                type="text"
                value={backtestName}
                onChange={(e) => setBacktestName(e.target.value)}
                placeholder="백테스트 이름을 입력하세요"
                fullWidth
              />
            </div>

            <div className={styles.formGroup}>
              <label>리밸런싱 주기</label>
              <div className={styles.selectWrapper}>
                <select
                  value={rebalancingPeriod}
                  onChange={(e) => setRebalancingPeriod(e.target.value)}
                  className={styles.select}
                >
                  <option value="주간">주간</option>
                  <option value="월간">월간</option>
                  <option value="분기">분기</option>
                  <option value="반기">반기</option>
                  <option value="연간">연간</option>
                </select>
                <div className={styles.selectIcon}>
                  <img src={imgFrame} alt="선택" />
                </div>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label>시작 날짜</label>
              <div className={styles.dateWrapper}>
                <input
                  type="text"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className={styles.dateInput}
                />
                <div className={styles.dateIcon}>
                  <img src={imgFrame1} alt="달력" />
                </div>
              </div>
            </div>

            <div className={styles.formGroup}>
              <label>종료 날짜</label>
              <div className={styles.dateWrapper}>
                <input
                  type="text"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className={styles.dateInput}
                />
                <div className={styles.dateIcon}>
                  <img src={imgFrame2} alt="달력" />
                </div>
              </div>
            </div>
          </div>

          <Button 
            variant="primary" 
            size="lg"
            onClick={handleRunBacktest}
            icon={<img src={imgFrame3} alt="실행" />}
          >
            백테스트 실행
          </Button>
        </div>

        <div className={styles.contentGrid}>
          {/* 주식 검색 카드 */}
          <div className={styles.stockSearchCard}>
            <div className={styles.cardHeader}>
              <h4>주식 검색</h4>
            </div>

            <div className={styles.searchInputWrapper}>
              <div className={styles.searchInput}>
                <div className={styles.searchIcon}>
                  <img src={imgSvg} alt="검색" />
                </div>
                <input
                  type="text"
                  placeholder="종목명 또는 종목코드를 입력하세요"
                  className={styles.input}
                />
              </div>
            </div>

            <div className={styles.stockList}>
              {stockData.map((stock, index) => (
                <div key={index} className={styles.stockItem}>
                  <div className={styles.stockInfo}>
                    <div className={styles.stockMain}>
                      <span className={styles.stockName}>{stock.name}</span>
                      <span className={styles.stockCode}>{stock.code}</span>
                      <span className={styles.stockSector}>{stock.sector}</span>
                    </div>
                    <div className={styles.stockCategory}>{stock.category}</div>
                  </div>

                  <div className={styles.stockPrice}>
                    <div className={styles.priceMain}>
                      <span className={styles.price}>{stock.price}</span>
                      <span className={`${styles.change} ${styles[stock.changeType]}`}>
                        {stock.change}
                      </span>
                    </div>
                    <div className={styles.priceDetails}>
                      <span>거래량: {stock.volume}</span>
                      <span>시총: {stock.marketCap}</span>
                    </div>
                  </div>

                  <div className={styles.stockChart}>
                    {/* Chart would go here */}
                    <div className={styles.chartPlaceholder}></div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 포트폴리오 주식 카드 */}
          <div className={styles.portfolioCard}>
            <div className={styles.cardHeader}>
              <h4>나의 포트폴리오 주식</h4>
              {selectedPortfolio && (
                <span className={styles.selectedPortfolio}>선택된 포트폴리오: {selectedPortfolio}</span>
              )}
            </div>

            <div className={styles.portfolioTable}>
              <div className={styles.tableHeader}>
                <div className={styles.headerCell}>종목명</div>
                <div className={styles.headerCell}>매수가</div>
                <div className={styles.headerCell}>수량(주)</div>
                <div className={styles.headerCell}>목표 비중(%)</div>
                <div className={styles.headerCell}>평가금액</div>
                <div className={styles.headerCell}>임계값(%)</div>
                <div className={styles.headerCell}>삭제</div>
              </div>

              {portfolioData.map((item, index) => (
                <div key={index} className={styles.tableRow}>
                  <div className={styles.tableCell}>
                    <span className={styles.stockName}>{item.name}</span>
                    <span className={styles.stockCode}>{item.code}</span>
                  </div>
                  <div className={styles.tableCell}>{item.buyPrice}</div>
                  <div className={styles.tableCell}>
                    <input
                      type="number"
                      value={item.quantity}
                      className={styles.numberInput}
                    />
                  </div>
                  <div className={styles.tableCell}>
                    <input
                      type="number"
                      value={item.targetWeight}
                      className={styles.numberInput}
                    />
                  </div>
                  <div className={styles.tableCell}>{item.currentValue}</div>
                  <div className={styles.tableCell}>
                    <input
                      type="number"
                      value={item.threshold}
                      className={styles.numberInput}
                    />
                  </div>
                  <div className={styles.tableCell}>
                    <button className={styles.deleteButton}>
                      <svg width="17" height="17" viewBox="0 0 17 17" fill="none">
                        <path d="M2.125 4.25H14.875" stroke="#999999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M6.375 4.25V2.125C6.375 1.71079 6.70829 1.375 7.125 1.375H9.875C10.2917 1.375 10.625 1.71079 10.625 2.125V4.25M12.75 4.25V14.875C12.75 15.2917 12.4167 15.625 12 15.625H5C4.58333 15.625 4.25 15.2917 4.25 14.875V4.25H12.75Z" stroke="#999999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M7.125 7.125V12.75" stroke="#999999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                        <path d="M9.875 7.125V12.75" stroke="#999999" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
                      </svg>
                    </button>
                  </div>
                </div>
              ))}
            </div>

            <div className={styles.portfolioSummary}>
              <span>목표 비중 합계: </span>
              <span className={styles.summaryGreen}>100% </span>
              <span>총 금액 합계: </span>
              <span className={styles.summaryGreen}>100만원</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}