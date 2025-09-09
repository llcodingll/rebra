import { useState } from 'react';
import styles from './DashboardPage.module.css';
import DashBoardSettingsTab from './DashBoardSettingsTab';
import AssetPortfolioChart from '../../widget/dashboard/AssetPortfolioChart';
import AssetTable from '../../widget/dashboard/AssetTable';
import ProfitStatusPage from './ProfitStatusPage';
import PortfolioSelectionModal from '../../widget/portfolio/PortfolioSelectionModal';

interface Portfolio {
  id: string;
  name: string;
  return: string;
  stockCount: number;
  createdDate: string;
  returnPositive: boolean;
  description?: string;
}

export default function DashboardPage() {
  const [activeSubTab, setActiveSubTab] = useState<'assets' | 'profit'>('assets');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedPortfolio, setSelectedPortfolio] = useState<Portfolio>({
    id: 'portfolio-1',
    name: '삼성전자 + SK하이닉스 포트폴리오',
    return: '+24.5%',
    stockCount: 3,
    createdDate: '2024-01-15',
    returnPositive: true,
    description: '반도체 대장주 중심'
  });

  const handlePortfolioLinkClick = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const portfolios: Portfolio[] = [
    {
      id: 'portfolio-1',
      name: '삼성전자 + SK하이닉스 포트폴리오',
      return: '+24.5%',
      stockCount: 3,
      createdDate: '2024-01-15',
      returnPositive: true,
      description: '반도체 대장주 중심'
    },
    {
      id: 'portfolio-2',
      name: '배당 중심 포트폴리오',
      return: '+18.2%',
      stockCount: 5,
      createdDate: '2024-02-10',
      returnPositive: true,
      description: '안정적인 배당 수익'
    },
    {
      id: 'portfolio-3',
      name: '성장주 포트폴리오',
      return: '+32.8%',
      stockCount: 8,
      createdDate: '2024-03-05',
      returnPositive: true,
      description: '고성장 기업 투자'
    },
    {
      id: 'portfolio-4',
      name: '안전자산 포트폴리오',
      return: '+12.1%',
      stockCount: 4,
      createdDate: '2024-01-20',
      returnPositive: true,
      description: '리스크 최소화'
    },
    {
      id: 'portfolio-5',
      name: '테크주 포트폴리오',
      return: '+28.9%',
      stockCount: 6,
      createdDate: '2024-02-28',
      returnPositive: true,
      description: '기술 혁신 기업'
    },
    {
      id: 'portfolio-6',
      name: '글로벌 포트폴리오',
      return: '-5.2%',
      stockCount: 12,
      createdDate: '2024-03-15',
      returnPositive: false,
      description: '해외 주식 분산투자'
    }
  ];

  const handlePortfolioSelect = (portfolioId: string) => {
    const selected = portfolios.find(p => p.id === portfolioId);
    if (selected) {
      setSelectedPortfolio(selected);
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
            <h2>{selectedPortfolio.name}</h2>
            <span className={styles.portfolioDesc}>{selectedPortfolio.description}</span>
          </div>
          <div className={styles.portfolioInfo}>
            <button className={styles.portfolioLink} onClick={handlePortfolioLinkClick}>
              다른 포트폴리오 보기
            </button>
          </div>
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
        portfolios={portfolios}
      />
    </div>
    </div>
  );
}