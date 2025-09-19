import { useState, useMemo, useEffect } from 'react';
import styles from './DashboardPage.module.css';
import { useApi } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import { transformPortfolioData, transformPortfolioDetailToStocks } from '../../features/portfolio/utils/portfolioTransform';
import type { Portfolio, Stock } from '../../entities/portfolio';
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

  // 포트폴리오 있음 상태일 때 기본값 설정 (portfolios 배열의 첫 번째 항목)
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null);

  // API로 포트폴리오 목록 조회
  const { data: portfolioData, isLoading: isPortfolioLoading, error: portfolioError, refetch: refetchPortfolios } = useApi({
    queryKey: ['portfolios'],
    apiFunction: () => portfolioApi.getPortfolioList(),
  });

  // 선택된 포트폴리오의 상세 정보 조회
  const { data: portfolioDetailData, isLoading: isDetailLoading, error: detailError, refetch: refetchPortfolioDetail } = useApi({
    queryKey: ['portfolio-detail', selectedPortfolio?.id],
    apiFunction: () => selectedPortfolio ? portfolioApi.getPortfolioDetail(Number(selectedPortfolio.id)) : Promise.reject('No portfolio selected'),
    enabled: !!selectedPortfolio?.id,
  });

  // 포트폴리오 상세 조회 상태 로깅
  console.log("=== 포트폴리오 상세 조회 상태 ===");
  console.log("selectedPortfolio:", selectedPortfolio);
  console.log("isDetailLoading:", isDetailLoading);
  console.log("portfolioDetailData:", portfolioDetailData);
  console.log("detailError:", detailError);

  // API 데이터를 기존 Portfolio 타입으로 변환
  const portfolios = useMemo(() => {
    if (!portfolioData?.portfolios) return [];
    return transformPortfolioData(portfolioData.portfolios);
  }, [portfolioData]);

  // 포트폴리오 존재 여부는 API 데이터로 판단
  const hasPortfolio = portfolios.length > 0 && !isPortfolioLoading;

  // API 데이터 로드 후 첫 번째 포트폴리오 선택
  useEffect(() => {
    if (portfolios.length > 0 && !selectedPortfolio) {
      setSelectedPortfolio(portfolios[0]);
    }
  }, [portfolios, selectedPortfolio]);
  
  // 개발용 토글 함수 제거 (API 기반으로 동작)

  // API에서 받은 주식 데이터를 기존 형식으로 변환
  const stockData: Stock[] = useMemo(() => {
    console.log("=== stockData 변환 시작 ===");
    console.log("portfolioDetailData:", portfolioDetailData);

    if (!portfolioDetailData) {
      console.log("portfolioDetailData가 없어서 빈 배열 반환");
      return [];
    }

    return transformPortfolioDetailToStocks(portfolioDetailData);
  }, [portfolioDetailData]);

  const handlePortfolioLinkClick = () => {
    setIsModalOpen(true);
  };

  const handleCreatePortfolio = () => {
    setIsCreateModalOpen(true);
  };

  const handleCreateModalClose = () => {
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
        return <ProfitPortfolioChart data={registeredStocks} portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined} />;
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

  // 에러 상태 처리
  if (portfolioError) {
    return (
      <div className={styles.dashboard}>
        <div>포트폴리오 목록을 불러오는데 실패했습니다.</div>
        <button onClick={() => window.location.reload()}>다시 시도</button>
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
          onSuccess={() => {
            // 포트폴리오 생성 성공 시 목록 새로고침
            refetchPortfolios();
          }}
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

          <DashBoardSettingsTab
            portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined}
            initialAutoRebalancing={portfolioDetailData?.portfolio?.autoRebalance || false}
            isLoadingSettings={isDetailLoading}
            onAutoRebalancingChanged={() => {
              // 자동 리밸런싱 설정 변경 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
            onRebalancingExecuted={() => {
              // 리밸런싱 실행 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
          />

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
              히스토리
            </button>
          </div>

          {renderContent()}

          {/* 자산 테이블들 */}
          <div className={styles.tablesContainer}>
          <AssetTable
            title="등록 주식"
            type="registered"
            data={stockData.filter(stock => stock.type === 'registered')}
            portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined}
            onStockRemoved={() => {
              // 주식 삭제 성공 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
            onStockSettingsUpdated={() => {
              // 주식 설정 업데이트 성공 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
          />
          <AssetTable
            title="미등록 주식"
            type="unregistered"
            data={stockData.filter(stock => stock.type === 'unregistered')}
            portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined}
            onStockRegistered={() => {
              // 주식 등록 성공 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
          />
          </div>
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
          onSuccess={() => {
            // 포트폴리오 생성 성공 시 목록 새로고침
            refetchPortfolios();
          }}
        />
      </div>
    </div>
  );
}