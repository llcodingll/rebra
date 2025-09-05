import { useState } from 'react';
import styles from './BacktestPage.module.css';
import { legacyBacktestData } from '../mocks/backtest';
import { imgFrame, imgFrame1, imgFrame2, imgFrame3 } from '../imports/svg-uh39g';
import PortfolioSelectionModal from '../components/PortfolioSelectionModal';
import BacktestCreationPage from './BacktestCreationPage';
import BacktestResultsPage from './BacktestResultsPage';

export default function BacktestPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showCreationPage, setShowCreationPage] = useState(false);
  const [showResultsPage, setShowResultsPage] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState<string | null>(null);
  const [selectedBacktest, setSelectedBacktest] = useState<any>(null);

  const handlePortfolioModalOpen = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    // 포트폴리오 선택 로직
    let portfolioName = 'A 포트폴리오';
    switch (portfolioId) {
      case 'portfolio-1':
        portfolioName = '삼성전자 + SK하이닉스 포트폴리오';
        break;
      case 'portfolio-2':
        portfolioName = '배당 중심 포트폴리오';
        break;
      case 'portfolio-3':
        portfolioName = '성장주 포트폴리오';
        break;
      case 'portfolio-4':
        portfolioName = '안전자산 포트폴리오';
        break;
      case 'portfolio-5':
        portfolioName = '테크주 포트폴리오';
        break;
    }
    setSelectedPortfolio(portfolioName);
    setShowCreationPage(true);
  };

  const handleDirectCreation = () => {
    setSelectedPortfolio(null);
    setShowCreationPage(true);
  };

  const handleBackToList = () => {
    setShowCreationPage(false);
    setShowResultsPage(false);
    setSelectedPortfolio(null);
    setSelectedBacktest(null);
  };

  const handleBacktestClick = (backtest: any) => {
    setSelectedBacktest(backtest);
    setShowResultsPage(true);
  };

  // 백테스트 결과 페이지 표시
  if (showResultsPage) {
    return (
      <BacktestResultsPage 
        onBack={handleBackToList}
        backtestData={selectedBacktest}
      />
    );
  }

  // 백테스트 생성 페이지 표시
  if (showCreationPage) {
    return (
      <BacktestCreationPage 
        onBack={handleBackToList}
        selectedPortfolio={selectedPortfolio || undefined}
      />
    );
  }

  return (
    <div className={styles.backtest}>
      <div className={styles.container}>
        {/* 백테스트 검색/생성 섹션 */}
        <div className={styles.searchSection}>
          <div className={styles.searchCard}>
            <h3>백테스트 검색</h3>
            
            <div className={styles.searchControls}>
              {/* 검색창 */}
              <div className={styles.searchInput}>
                <div className={styles.searchIcon}>
                  <img src={imgFrame2} alt="검색" />
                </div>
                <input 
                  type="text" 
                  placeholder="백테스트 이름을 입력하세요"
                  className={styles.input}
                />
              </div>

              {/* 생성 버튼들 */}
              <div className={styles.createButtons}>
                <button className={styles.createFromPortfolio} onClick={handlePortfolioModalOpen}>
                  <img src={imgFrame1} alt="업로드" />
                  내 포트폴리오에서 가져오기
                </button>
                
                <button className={styles.createDirect} onClick={handleDirectCreation}>
                  <img src={imgFrame} alt="추가" />
                  백테스트 직접 생성
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 백테스트 히스토리 */}
        <div className={styles.historySection}>
          <div className={styles.historyCard}>
            <h3>백테스트 히스토리</h3>
            
            <div className={styles.tableWrapper}>
              <table className={styles.table}>
                <thead>
                  <tr className={styles.headerRow}>
                    <th>백테스트 이름</th>
                    <th>생성일</th>
                    <th>백테스트 기간</th>
                    <th>총 수익률</th>
                    <th>최대낙폭</th>
                    <th>샤프비율</th>
                    <th>상태</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {legacyBacktestData.map((item, index) => (
                    <tr 
                      key={index} 
                      className={`${styles.dataRow} ${styles.clickable}`}
                      onClick={() => handleBacktestClick(item)}
                    >
                      <td className={styles.nameCell}>
                        {item.name}
                      </td>
                      <td className={styles.dateCell}>
                        {item.date}
                      </td>
                      <td className={styles.periodCell}>
                        {item.period}
                      </td>
                      <td className={styles.returnCell}>
                        {item.totalReturn}
                      </td>
                      <td className={styles.drawdownCell}>
                        {item.maxDrawdown}
                      </td>
                      <td className={styles.sharpeCell}>
                        {item.sharpeRatio}
                      </td>
                      <td className={styles.statusCell}>
                        <span className={styles.statusBadge}>
                          {item.status}
                        </span>
                      </td>
                      <td className={styles.actionCell} onClick={(e) => e.stopPropagation()}>
                        <button className={styles.deleteButton}>
                          <img src={imgFrame3} alt="삭제" />
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* 페이지네이션 */}
        <div className={styles.pagination}>
          <button className={styles.paginationButton}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path fillRule="evenodd" clipRule="evenodd" d="M12.6325 11.8159C12.4028 12.0547 12.023 12.0622 11.7841 11.8325L8.18413 8.4325C8.06649 8.31938 8 8.16321 8 8C8 7.83679 8.06649 7.68062 8.18413 7.5675L11.7841 4.1675C12.023 3.93782 12.4028 3.94527 12.6325 4.18413C12.8622 4.423 12.8547 4.80282 12.6159 5.0325L9.46566 8L12.6159 10.9675C12.8547 11.1972 12.8622 11.577 12.6325 11.8159ZM7.8325 11.8159C7.60282 12.0547 7.223 12.0622 6.98413 11.8325L3.38413 8.4325C3.26649 8.31938 3.2 8.16321 3.2 8C3.2 7.83679 3.26649 7.68062 3.38413 7.5675L6.98413 4.1675C7.223 3.93782 7.60282 3.94527 7.8325 4.18414C8.06218 4.423 8.05473 4.80282 7.81586 5.0325L4.66566 8L7.81587 10.9675C8.05473 11.1972 8.06218 11.577 7.8325 11.8159Z" fill="#626262"/>
            </svg>
            First
          </button>
          
          <button className={styles.paginationButton}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path fillRule="evenodd" clipRule="evenodd" d="M10.2325 4.18414C10.4622 4.423 10.4547 4.80282 10.2159 5.0325L7.06566 8L10.2159 10.9675C10.4547 11.1972 10.4622 11.577 10.2325 11.8159C10.0028 12.0547 9.623 12.0622 9.38413 11.8325L5.78413 8.4325C5.66649 8.31938 5.6 8.16321 5.6 8C5.6 7.83679 5.66649 7.68062 5.78413 7.5675L9.38413 4.1675C9.623 3.93783 10.0028 3.94527 10.2325 4.18414Z" fill="#626262"/>
            </svg>
            Back
          </button>

          <button className={styles.pageNumber}>1</button>
          <button className={`${styles.pageNumber} ${styles.active}`}>2</button>
          <button className={styles.pageNumber}>3</button>
          <button className={styles.pageNumber}>4</button>
          <button className={styles.pageNumber}>
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2.4 8C2.4 7.33726 2.93726 6.8 3.6 6.8C4.26274 6.8 4.8 7.33726 4.8 8C4.8 8.66274 4.26274 9.2 3.6 9.2C2.93726 9.2 2.4 8.66274 2.4 8Z" fill="#313131"/>
              <path d="M6.8 8C6.8 7.33726 7.33726 6.8 8 6.8C8.66274 6.8 9.2 7.33726 9.2 8C9.2 8.66274 8.66274 9.2 8 9.2C7.33726 9.2 6.8 8.66274 6.8 8Z" fill="#313131"/>
              <path d="M12.4 6.8C11.7373 6.8 11.2 7.33726 11.2 8C11.2 8.66274 11.7373 9.2 12.4 9.2C13.0627 9.2 13.6 8.66274 13.6 8C13.6 7.33726 13.0627 6.8 12.4 6.8Z" fill="#313131"/>
            </svg>
          </button>
          <button className={styles.pageNumber}>25</button>

          <button className={styles.paginationButton}>
            Next
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path fillRule="evenodd" clipRule="evenodd" d="M5.7675 11.8159C5.53783 11.577 5.54527 11.1972 5.78414 10.9675L8.93434 8L5.78414 5.0325C5.54527 4.80282 5.53782 4.423 5.7675 4.18413C5.99718 3.94527 6.377 3.93782 6.61587 4.1675L10.2159 7.5675C10.3335 7.68062 10.4 7.83679 10.4 8C10.4 8.16321 10.3335 8.31938 10.2159 8.4325L6.61587 11.8325C6.377 12.0622 5.99718 12.0547 5.7675 11.8159Z" fill="#626262"/>
            </svg>
          </button>

          <button className={styles.paginationButton}>
            Last
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M3.38414 10.9675C3.14527 11.1972 3.13783 11.577 3.3675 11.8159C3.59718 12.0547 3.977 12.0622 4.21587 11.8325L7.81587 8.4325C7.93351 8.31938 8 8.16321 8 8C8 7.83679 7.93351 7.68062 7.81587 7.5675L4.21587 4.1675C3.977 3.93783 3.59718 3.94527 3.3675 4.18414C3.13783 4.423 3.14527 4.80282 3.38414 5.0325L6.53434 8L3.38414 10.9675Z" fill="#626262"/>
              <path d="M8.18414 10.9675C7.94527 11.1972 7.93783 11.577 8.1675 11.8159C8.39718 12.0547 8.777 12.0622 9.01587 11.8325L12.6159 8.4325C12.7335 8.31938 12.8 8.16321 12.8 8C12.8 7.83679 12.7335 7.68062 12.6159 7.5675L9.01587 4.1675C8.777 3.93783 8.39718 3.94527 8.1675 4.18414C7.93783 4.423 7.94527 4.80282 8.18414 5.0325L11.3343 8L8.18414 10.9675Z" fill="#626262"/>
            </svg>
          </button>
        </div>
      </div>

      <PortfolioSelectionModal 
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSelect={handlePortfolioSelect}
      />
    </div>
  );
}