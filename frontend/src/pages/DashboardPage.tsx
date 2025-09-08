import { useState } from 'react';
import styles from './DashboardPage.module.css';
import PortfolioChart from '../widget/dashboard/PortfolioChart';
import AssetTable from '../widget/dashboard/AssetTable';
import ProfitStatusPage from './ProfitStatusPage';
import PortfolioSelectionModal from '../widget/portfolio/PortfolioSelectionModal';

export default function DashboardPage() {
  const [activeSubTab, setActiveSubTab] = useState<'assets' | 'profit'>('assets');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState('A 포트폴리오');

  const handlePortfolioLinkClick = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    // 포트폴리오 선택 로직
    switch (portfolioId) {
      case 'portfolio-1':
        setSelectedPortfolio('삼성전자 + SK하이닉스 포트폴리오');
        break;
      case 'portfolio-2':
        setSelectedPortfolio('배당 중심 포트폴리오');
        break;
      case 'portfolio-3':
        setSelectedPortfolio('성장주 포트폴리오');
        break;
      case 'portfolio-4':
        setSelectedPortfolio('안전자산 포트폴리오');
        break;
      case 'portfolio-5':
        setSelectedPortfolio('테크주 포트폴리오');
        break;
      default:
        setSelectedPortfolio('A 포트폴리오');
    }
  };

  const renderContent = () => {
    switch (activeSubTab) {
      case 'assets':
        return (
          <>
            {/* 리밸런싱 컨트롤 */}
            <div className={styles.rebalancingControls}>
              <div className={styles.controlGroup}>
                <h3>즉시 실행</h3>
                <button className={styles.executeButton}>
                  <svg width="19" height="19" viewBox="0 0 19 19" fill="none">
                    <path d="M4.75589 2.38672L15.7495 9.33007L4.75589 16.2734V2.38672Z" fill="white" stroke="white" strokeWidth="1.38867" strokeLinecap="round" strokeLinejoin="round"/>
                  </svg>
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
          </>
        );
      case 'profit':
        return <ProfitStatusPage />;
      default:
        return null;
    }
  };

  return (
    <div className={styles.dashboard}>
      <div className={styles.container}>
        {/* 포트폴리오 선택 섹션 */}
        <div className={styles.portfolioHeader}>
          <div className={styles.portfolioTitle}>
            <h2>{selectedPortfolio}</h2>
            <span className={styles.portfolioDesc}>은퇴 자금 마련</span>
          </div>
          <button className={styles.portfolioLink} onClick={handlePortfolioLinkClick}>
            다른 포트폴리오 보기
          </button>
        </div>

        {/* 포트폴리오 차트 - 탭 기능 포함 */}
        <PortfolioChart activeTab={activeSubTab} onTabChange={setActiveSubTab} />

        {renderContent()}
      </div>

              {/* 자산 테이블들 */}
        <div className={styles.tablesContainer}>
          <AssetTable title="등록 주식" type="registered" />
          <AssetTable title="미등록 주식" type="unregistered" />
        </div>

      <PortfolioSelectionModal 
        isOpen={isModalOpen}
        onClose={handleModalClose}
        onSelect={handlePortfolioSelect}
      />
    </div>
  );
}