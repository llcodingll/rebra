import { useState, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styles from './DashboardPage.module.css';
import { portfolioList, type Portfolio } from '../../mocks/portfolio';
import { dashboardStockData } from '../../mocks/dashboardStocks';
import { type PortfolioCreateData } from '../../mocks/portfolioCreate';
import DashBoardSettingsTab from '../../widgets/dashboard/DashBoardSettingsTab';
import AssetPortfolioChart from '../../widgets/dashboard/AssetPortfolioChart';
import AssetTable from '../../widgets/dashboard/AssetTable';
import ProfitPortfolioChart from '../../widgets/dashboard/ProfitPortfolioChart';
import PortfolioSelectionModal from '../../widgets/portfolio/PortfolioSelectionModal';
import NoPortfolioState from '../../widgets/dashboard/NoPortfolioState';
import PortfolioCreateModal from '../../widgets/portfolio/PortfolioCreateModal';
import PortfolioHeader from '../../widgets/dashboard/PortfolioHeader';


export default function DashboardPage() {
  const [activeSubTab, setActiveSubTab] = useState<'assets' | 'profit'>('assets');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  
  const portfolios = portfolioList;

  // 포트폴리오 상태 테스트용 - 아래 두 줄 중 하나만 주석 해제하여 테스트
  const [hasPortfolio, setHasPortfolio] = useState(true); // 포트폴리오 없음 상태 테스트
  // const [hasPortfolio, setHasPortfolio] = useState(true); // 포트폴리오 있음 상태 테스트
  
  // 포트폴리오 있음 상태일 때 기본값 설정 (portfolios 배열의 첫 번째 항목)
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(
    portfolioList[0]
  );
  
  // 개발용 포트폴리오 상태 토글 함수
  const togglePortfolioState = () => {
    setHasPortfolio(prev => !prev);
    if (!hasPortfolio) {
      setSelectedPortfolio(portfolioList[0]);
    } else {
      setSelectedPortfolio(null);
    }
  };

  const stockData = dashboardStockData;

  const handlePortfolioLinkClick = () => {
    setIsModalOpen(true);
  };

  const handleCreatePortfolio = () => {
    setIsCreateModalOpen(true);
  };

  const handleCreateModalClose = () => {
    setIsCreateModalOpen(false);
  };

  const handlePortfolioCreate = (portfolioData: PortfolioCreateData) => {
    console.log('새 포트폴리오 생성:', portfolioData);
    // 실제 API 호출 및 포트폴리오 생성 로직 구현 예정
    
    // 생성 후 상태 업데이트
    setHasPortfolio(true);
    // 추후 실제 포트폴리오 데이터로 selectedPortfolio 설정
    
    // 모달 닫기
    setIsCreateModalOpen(false);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    const selected = portfolioList.find(p => p.id === portfolioId);
    if (selected) {
      setSelectedPortfolio(selected);
      setHasPortfolio(true);
    }
  };

  // 등록된 주식 데이터 메모이제이션
  const registeredStocks = useMemo(() => 
    stockData.filter(stock => stock.type === 'registered'), 
    [stockData]
  );

  const renderContent = () => {
    switch (activeSubTab) {
      case 'assets':
        return <AssetPortfolioChart data={registeredStocks} />;
      case 'profit':
        return <ProfitPortfolioChart data={registeredStocks} />;
      default:
        return null;
    }
  };

  // 포트폴리오가 없으면 NoPortfolioState 컴포넌트 렌더링
  if (!hasPortfolio) {
    return (
      <div className={styles.dashboard}>
        {/* 개발용 토글 버튼 */}
        <button 
          onClick={togglePortfolioState}
          style={{
            position: 'fixed',
            bottom: '20px',
            right: '20px',
            zIndex: 9999,
            padding: '4px 8px',
            fontSize: '10px',
            backgroundColor: '#2563eb',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          {hasPortfolio ? '포트폴리오 있음' : '포트폴리오 없음'}
        </button>
        
        <NoPortfolioState onCreatePortfolio={handleCreatePortfolio} />
        <PortfolioSelectionModal 
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onSelect={handlePortfolioSelect}
          portfolios={portfolioList}
          onCreatePortfolio={handleCreatePortfolio}
        />
        <PortfolioCreateModal
          isOpen={isCreateModalOpen}
          onClose={handleCreateModalClose}
          onCreatePortfolio={handlePortfolioCreate}
        />
      </div>
    );
  }

  return (
    <div>
      <div className={styles.dashboard}>
        {/* 개발용 토글 버튼 */}
        <button 
          onClick={togglePortfolioState}
          style={{
            position: 'fixed',
            bottom: '20px',
            right: '20px',
            zIndex: 9999,
            padding: '4px 8px',
            fontSize: '10px',
            backgroundColor: '#2563eb',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          {hasPortfolio ? '포트폴리오 있음' : '포트폴리오 없음'}
        </button>
        
        <div className={styles.container}>
          {/* 포트폴리오 선택 섹션 */}
          <PortfolioHeader
            selectedPortfolio={selectedPortfolio}
            onPortfolioLinkClick={handlePortfolioLinkClick}
          />

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
          <AssetTable 
            title="등록 주식" 
            type="registered" 
            data={stockData.filter(stock => stock.type === 'registered')}
          />
          <AssetTable 
            title="미등록 주식" 
            type="unregistered" 
            data={stockData.filter(stock => stock.type === 'unregistered')}
          />
        </div>

        <PortfolioSelectionModal 
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onSelect={handlePortfolioSelect}
          portfolios={portfolioList}
          onCreatePortfolio={handleCreatePortfolio}
        />
        <PortfolioCreateModal
          isOpen={isCreateModalOpen}
          onClose={handleCreateModalClose}
          onCreatePortfolio={handlePortfolioCreate}
        />
      </div>
    </div>
  );
}