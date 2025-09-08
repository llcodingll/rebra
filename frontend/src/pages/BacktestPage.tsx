import { useState } from 'react';
import styles from './BacktestPage.module.css';
import { legacyBacktestData } from '../mocks/backtest';
import { imgFrame, imgFrame1, imgFrame2, imgFrame3 } from '../assets/imports/svg-uh39g';
import PortfolioSelectionModal from '../widget/portfolio/PortfolioSelectionModal';
import BacktestCreationPage from './BacktestCreationPage';
import BacktestResultsPage from './BacktestResultsPage';
import Pagination from '../widget/common/Pagination';

export default function BacktestPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [showCreationPage, setShowCreationPage] = useState(false);
  const [showResultsPage, setShowResultsPage] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState<string | null>(null);
  const [selectedBacktest, setSelectedBacktest] = useState<any>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;
  const totalPages = Math.ceil(legacyBacktestData.length / itemsPerPage);

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
                  {legacyBacktestData
                    .slice((currentPage - 1) * itemsPerPage, currentPage * itemsPerPage)
                    .map((item, index) => (
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

        <Pagination
          currentPage={currentPage}
          totalPages={totalPages}
          onPageChange={setCurrentPage}
        />
      </div>

      <PortfolioSelectionModal 
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSelect={handlePortfolioSelect}
      />
    </div>
  );
}