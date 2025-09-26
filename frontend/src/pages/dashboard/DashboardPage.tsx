import { useState, useMemo, useEffect, useCallback } from 'react';
import { useOutletContext } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';
import styles from './DashboardPage.module.css';
import { useApi } from '../../shared/hook/useApi';
import { isMarketOpen } from '../../shared/util/marketTime';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import { transformPortfolioData, transformPortfolioDetailToStocks } from '../../features/portfolio/utils/portfolioTransform';
import type { Portfolio, Stock } from '../../entities/portfolio';
import { useAccountStore } from '../../entities/account/accountStore';
import DashBoardSettingsTab from '../../widgets/dashboard/DashBoardSettingsTab';
import AssetPortfolioChart from '../../widgets/dashboard/AssetPortfolioChart';
import AssetTable from '../../widgets/dashboard/AssetTable';
import ProfitPortfolioChart from '../../widgets/dashboard/ProfitPortfolioChart';
import PortfolioSelectionModal from '../../widgets/portfolio/PortfolioSelectionModal';
import NoPortfolioState from '../../widgets/dashboard/NoPortfolioState';
import PortfolioCreateModal from '../../widgets/portfolio/PortfolioCreateModal';
import PortfolioHeader from '../../widgets/dashboard/PortfolioHeader';
import TutorialOverlay from '../../widgets/tutorial/TutorialOverlay';
import { dashboardTutorialSteps } from '../../widgets/tutorial/dashboardTutorialSteps';

/**
 * 대시보드 메인 페이지 컴포넌트
 * - 포트폴리오 목록 조회 및 선택
 * - 포트폴리오 상세 정보 표시 (자산 현황, 히스토리)
 * - 자동 리밸런싱 설정 및 수동 리밸런싱 실행
 * - 주식 등록/삭제 관리
 * - 실시간 데이터 폴링 (시장 시간 중)
 */
export default function DashboardPage() {
  // 튜토리얼 관련
  const { tutorialStates, closeTutorial } = useOutletContext<{
    tutorialStates: { dashboard: boolean; search: boolean; backtest: boolean };
    closeTutorial: (page: 'dashboard' | 'search' | 'backtest') => void;
  }>();

  // UI 상태 관리
  const [activeSubTab, setActiveSubTab] = useState<'assets' | 'profit'>('assets'); // 현재 활성 탭 (자산/수익률)
  const [isModalOpen, setIsModalOpen] = useState(false); // 포트폴리오 선택 모달
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false); // 포트폴리오 생성 모달
  const [isEditingWeights, setIsEditingWeights] = useState(false); // 비중 편집 중 여부 (폴링 제어용)

  // 현재 선택된 포트폴리오 상태 (portfolios 배열의 첫 번째 항목이 기본값)
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio | null>(null);

  // 계정 정보 store
  const { setAccountId } = useAccountStore();

  // React Query client
  const queryClient = useQueryClient();

  // API로 포트폴리오 목록 조회
  const { data: portfolioData, isLoading: isPortfolioLoading, error: portfolioError, refetch: refetchPortfolios } = useApi({
    queryKey: ['portfolios'],
    apiFunction: () => portfolioApi.getPortfolioList(),
  });

  // 선택된 포트폴리오의 상세 정보 조회 (시장 시간에만 30초마다 자동 새로고침)
  const { data: portfolioDetailData, isLoading: isDetailLoading, error: detailError, refetch: refetchPortfolioDetail } = useApi({
    queryKey: ['portfolio-detail', selectedPortfolio?.id],
    apiFunction: () => selectedPortfolio ? portfolioApi.getPortfolioDetail(Number(selectedPortfolio.id)) : Promise.reject('No portfolio selected'),
    enabled: !!selectedPortfolio?.id, // 첫 조회는 항상 실행
    //refetchInterval: 1000, // 항상 1초마다 polling (테스트용)
    refetchInterval: (isMarketOpen() && !isEditingWeights) ? 1000 : false, 
    refetchIntervalInBackground: true, // 백그라운드에서도 새로고침
  });

  // 포트폴리오 상세 조회 상태 로깅
  console.log("=== 포트폴리오 상세 조회 상태 ===");
  console.log("selectedPortfolio:", selectedPortfolio);
  console.log("isDetailLoading:", isDetailLoading);
  console.log("portfolioDetailData:", portfolioDetailData);
  console.log("detailError:", detailError);

  // 포트폴리오 상세 데이터가 로드되면 accountId를 store에 저장
  useEffect(() => {
    if (portfolioDetailData?.portfolio?.account?.id) {
      setAccountId(portfolioDetailData.portfolio.account.id);
      console.log("AccountId 저장됨:", portfolioDetailData.portfolio.account.id);
    }
  }, [portfolioDetailData?.portfolio?.account?.id, setAccountId]);

  // API 데이터를 기존 Portfolio 타입으로 변환
  const portfolios = useMemo(() => {
    if (!portfolioData?.portfolios) return [];
    return transformPortfolioData(portfolioData.portfolios);
  }, [portfolioData]);

  // 포트폴리오 존재 여부 판단 (로딩 완료 후 데이터 유무로 결정)
  const hasPortfolio = portfolios.length > 0;
  const shouldShowNoPortfolio = !isPortfolioLoading && portfolios.length === 0;

  // API 데이터 로드 후 첫 번째 포트폴리오 선택
  useEffect(() => {
    if (portfolios.length > 0 && !selectedPortfolio) {
      setSelectedPortfolio(portfolios[0]);
    }
  }, [portfolios, selectedPortfolio]);
  
  // 개발용 토글 함수 제거됨 - 현재는 API 기반으로만 동작

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

  // 포트폴리오 선택 모달 열기
  const handlePortfolioLinkClick = () => {
    setIsModalOpen(true);
  };

  // 포트폴리오 생성 모달 열기
  const handleCreatePortfolio = () => {
    setIsCreateModalOpen(true);
  };

  // 포트폴리오 생성 모달 닫기
  const handleCreateModalClose = () => {
    setIsCreateModalOpen(false);
  };

  // 포트폴리오 선택 모달 닫기
  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    const selected = portfolios.find(p => p.id === portfolioId);
    if (selected) {
      setSelectedPortfolio(selected);
    }
  };

  // 튜토리얼 오버레이 닫기
  const handleTutorialClose = useCallback(() => {
    closeTutorial('dashboard');
  }, [closeTutorial]);

  // 등록된 주식 데이터 메모이제이션
  const registeredStocks = useMemo(() => 
    stockData.filter(stock => stock.type === 'registered'), 
    [stockData]
  );

  // 활성 탭에 따른 메인 컨텐츠 렌더링
  const renderContent = () => {
    // 로딩 중이거나 포트폴리오 데이터가 아직 없을 때만 로딩 표시
    if (isDetailLoading || !portfolioDetailData) {
      return (
        <div style={{
          height: '550px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: '#666',
          backgroundColor: '#f9f9f9',
          borderRadius: '8px',
          margin: '20px 0'
        }}>
          데이터를 불러오는 중...
        </div>
      );
    }

    switch (activeSubTab) {
      case 'assets':
        return <AssetPortfolioChart data={registeredStocks} />;
      case 'profit':
        return <ProfitPortfolioChart data={registeredStocks} portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined} />;
      default:
        return null;
    }
  };

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
  if (shouldShowNoPortfolio) {
    return (
      <div className={styles.dashboard}>
        {/* 포트폴리오가 없을 때 표시되는 빈 상태 컴포넌트 */}
        <NoPortfolioState onCreatePortfolio={handleCreatePortfolio} />

        {/* 포트폴리오 선택 모달 */}
        <PortfolioSelectionModal
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onSelect={handlePortfolioSelect}
          portfolios={portfolios}
          onCreatePortfolio={handleCreatePortfolio}
        />

        {/* 포트폴리오 생성 모달 */}
        <PortfolioCreateModal
          isOpen={isCreateModalOpen}
          onClose={handleCreateModalClose}
          onSuccess={() => {
            // 포트폴리오 생성 성공 시 목록 새로고침
            refetchPortfolios();
          }}
        />

        {/* 튜토리얼 오버레이 - 포트폴리오 없어도 작동 */}
        <TutorialOverlay
          isOpen={tutorialStates.dashboard}
          onClose={handleTutorialClose}
          steps={dashboardTutorialSteps}
        />
      </div>
    );
  }

  // 메인 대시보드 UI 렌더링
  return (
    <div>
      <div className={styles.dashboard}>
        <div className={styles.container}>
          {/* 포트폴리오 선택 섹션 */}
          <PortfolioHeader
            selectedPortfolio={selectedPortfolio}
            onPortfolioLinkClick={handlePortfolioLinkClick}
          />

          {/* 포트폴리오 설정 탭 (자동 리밸런싱, 수동 리밸런싱) */}
          <DashBoardSettingsTab
            portfolioId={selectedPortfolio ? Number(selectedPortfolio.id) : undefined}
            initialAutoRebalancing={portfolioDetailData?.portfolio?.autoRebalance || false}
            isLoadingSettings={isDetailLoading}
            onAutoRebalancingChanged={() => {
              // 자동 리밸런싱 설정 변경 시 포트폴리오 상세 정보 새로고침
              refetchPortfolioDetail();
            }}
            onRebalancingExecuted={() => {
              // 리밸런싱 실행 시 포트폴리오 상세 정보 및 히스토리 관련 캐시 새로고침
              refetchPortfolioDetail();

              // 히스토리 관련 쿼리들 무효화
              if (selectedPortfolio?.id) {
                const portfolioId = Number(selectedPortfolio.id);
                queryClient.invalidateQueries({ queryKey: ['rebalancing-history-table', portfolioId] });
                queryClient.invalidateQueries({ queryKey: ['rebalancing-history', portfolioId] });
                queryClient.invalidateQueries({ queryKey: ['portfolio-performance', portfolioId] });
              }
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

          {/* 하단 자산 테이블들 (등록 주식 / 미등록 주식) */}
          <div className={styles.tablesContainer}>
            {/* 등록된 주식 테이블 (비중 설정 가능) */}
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
              onEditModeChange={setIsEditingWeights}
            />
            {/* 미등록 주식 테이블 (등록 가능) */}
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

        {/* 포트폴리오 선택 모달 */}
        <PortfolioSelectionModal
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onSelect={handlePortfolioSelect}
          portfolios={portfolios}
          onCreatePortfolio={handleCreatePortfolio}
        />
        {/* 포트폴리오 생성 모달 */}
        <PortfolioCreateModal
          isOpen={isCreateModalOpen}
          onClose={handleCreateModalClose}
          onSuccess={() => {
            // 포트폴리오 생성 성공 시 목록 새로고침
            refetchPortfolios();
          }}
        />
      </div>

      {/* 튜토리얼 오버레이 */}
      <TutorialOverlay
        isOpen={tutorialStates.dashboard}
        onClose={handleTutorialClose}
        steps={dashboardTutorialSteps}
      />
    </div>
  );
}