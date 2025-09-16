import { useState, useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import styles from './DashboardPage.module.css';
import { portfolioList, type Portfolio } from '../../mocks/portfolio';
import { dashboardStockData } from '../../mocks/dashboardStocks';
import { useApi } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
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

  // API로 포트폴리오 목록 조회
  const { data: portfolioData, isLoading: isPortfolioLoading, error: portfolioError } = useApi({
    queryKey: ['portfolios'],
    apiFunction: () => portfolioApi.getPortfolioList(),
  });

  // API 데이터를 기존 Portfolio 타입으로 변환
  const portfolios = useMemo(() => {
    if (!portfolioData?.portfolios) return [];

    return portfolioData.portfolios.map(item => ({
      id: item.portfolioId.toString(),
      name: item.name,
      description: item.description,
      stockCount: item.registeredStockCount,
      return: `${item.totalReturnRate > 0 ? '+' : ''}${item.totalReturnRate.toFixed(1)}%`,
      createdDate: item.createdAt.split('T')[0],
      returnPositive: item.totalReturnRate > 0,
      accountType: item.accountType,
    }));
  }, [portfolioData]);

  // 포트폴리오 존재 여부는 API 데이터로 판단
  const hasPortfolio = portfolios.length > 0 && !isPortfolioLoading;

  // 포트폴리오 있음 상태일 때 기본값 설정 (portfolios 배열의 첫 번째 항목)
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null);

  // API 데이터 로드 후 첫 번째 포트폴리오 선택
  useMemo(() => {
    if (portfolios.length > 0 && !selectedPortfolio) {
      setSelectedPortfolio(portfolios[0]);
    }
  }, [portfolios, selectedPortfolio]);
  
  // 개발용 토글 함수 제거 (API 기반으로 동작)

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
    const selected = portfolios.find(p => p.id === portfolioId);
    if (selected) {
      setSelectedPortfolio(selected);
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

  // 로딩 중이면 로딩 표시
  if (isPortfolioLoading) {
    return (
      <div className={styles.dashboard}>
        <div>포트폴리오 목록을 불러오는 중...</div>
      </div>
    );
  }

  // 포트폴리오가 없으면 NoPortfolioState 컴포넌트 렌더링
  if (!hasPortfolio) {
    return (
      <div className={styles.dashboard}>
        <NoPortfolioState onCreatePortfolio={handleCreatePortfolio} />
        <PortfolioSelectionModal
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onSelect={handlePortfolioSelect}
          portfolios={portfolios}
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
          portfolios={portfolios}
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