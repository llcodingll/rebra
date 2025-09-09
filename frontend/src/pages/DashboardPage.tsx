import { useState } from 'react';
import styles from './DashboardPage.module.css';
import DashBoardSettingsTab from './DashBoardSettingsTab';
import AssetPortfolioChart from '../widget/dashboard/AssetPortfolioChart';
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
        return <AssetPortfolioChart />;
      case 'profit':
        return <ProfitStatusPage />;
      default:
        return null;
    }
  };

  return (
    <div>
            {/* 리밸런싱 실행 설정*/}

          

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

        <DashBoardSettingsTab />

        {/* 탭 헤더 */}
        <div className={styles.tabHeader}>
          <button 
            className={`${styles.tab} ${activeSubTab === 'assets' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('assets')}
          >
            자산 현황
          </button>
          <button 
            className={`${styles.tab} ${activeSubTab === 'profit' ? styles.active : ''}`}
            onClick={() => setActiveSubTab('profit')}
          >
            수익률 현황
          </button>
        </div>

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
    </div>
  );
}