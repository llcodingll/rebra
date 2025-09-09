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

interface Stock {
  name: string;
  code: string;
  buyPrice: string;
  currentPrice: string;
  quantity: string;
  value: string;
  return: string;
  returnAmount: string;
  currentWeight: string;
  targetWeight: string;
  weight: string;
  threshold: string;
  type: 'registered' | 'unregistered';
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

  const [stockData] = useState<Stock[]>([
    {
      name: '삼성전자',
      code: '005930',
      buyPrice: '68,000',
      currentPrice: '71,800',
      quantity: '50주',
      value: '3,590,000원',
      return: '+5.6%',
      returnAmount: '(+190,000원)',
      currentWeight: '32.1%',
      targetWeight: '30',
      weight: '6',
      threshold: '10',
      type: 'registered'
    },
    {
      name: 'SK하이닉스',
      code: '000660',
      buyPrice: '85,000',
      currentPrice: '89,500',
      quantity: '30주',
      value: '2,685,000원',
      return: '+5.3%',
      returnAmount: '(+135,000원)',
      currentWeight: '24.0%',
      targetWeight: '25',
      weight: '5',
      threshold: '5',
      type: 'registered'
    },
    {
      name: 'LG에너지솔루션',
      code: '373220',
      buyPrice: '390,000',
      currentPrice: '412,000',
      quantity: '15주',
      value: '6,180,000원',
      return: '+5.6%',
      returnAmount: '(+330,000원)',
      currentWeight: '18.5%',
      targetWeight: '20',
      weight: '4',
      threshold: '10',
      type: 'registered'
    },
    {
      name: '삼성바이오로직스',
      code: '207940',
      buyPrice: '750,000',
      currentPrice: '789,000',
      quantity: '2주',
      value: '1,578,000원',
      return: '+5.2%',
      returnAmount: '(+78,000원)',
      currentWeight: '14.1%',
      targetWeight: '15',
      weight: '3',
      threshold: '5',
      type: 'registered'
    },
    {
      name: 'NAVER',
      code: '035420',
      buyPrice: '175,000',
      currentPrice: '183,500',
      quantity: '25주',
      value: '4,587,500원',
      return: '+4.9%',
      returnAmount: '(+212,500원)',
      currentWeight: '11.4%',
      targetWeight: '10',
      weight: '2',
      threshold: '3',
      type: 'unregistered'
    },
    {
      name: '카카오',
      code: '035720',
      buyPrice: '45,000',
      currentPrice: '48,200',
      quantity: '40주',
      value: '1,928,000원',
      return: '+7.1%',
      returnAmount: '(+128,000원)',
      currentWeight: '0%',
      targetWeight: '0',
      weight: '0',
      threshold: '0',
      type: 'unregistered'
    }
  ]);

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
        return <AssetPortfolioChart data={stockData.filter(stock => stock.type === 'registered')} />;
      case 'profit':
        return <ProfitStatusPage />;
      default:
        return null;
    }
  };

  return (
    <div>

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
      />
    </div>
    </div>
  );
}