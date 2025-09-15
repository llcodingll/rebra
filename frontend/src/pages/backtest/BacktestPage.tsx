import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import styles from './BacktestPage.module.css';
import { legacyBacktestData } from '../../mocks/backtest';
import PortfolioSelectionModal from '../../widgets/portfolio/PortfolioSelectionModal';
import Pagination from '../../widgets/common/Pagination';
import BacktestSearchWidget from '../../widgets/backtest/BacktestSearchWidget';
import BacktestHistoryWidget from '../../widgets/backtest/BacktestHistoryWidget';

export default function BacktestPage() {
  interface Portfolio {
    id: string;
    name: string;
    return: string;
    stockCount: number;
    createdDate: string;
    returnPositive: boolean;
    description?: string;
  }
  const navigate = useNavigate();
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentPage, setCurrentPage] = useState(1);
  const [backtestData, setBacktestData] = useState(legacyBacktestData);
  const itemsPerPage = 10;
  const totalPages = Math.ceil(backtestData.length / itemsPerPage);
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

  

  const handlePortfolioModalOpen = () => {
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
  };

  const handlePortfolioSelect = (portfolioId: string) => {
    navigate('/backtest/create', { state: { portfolioId } });
  };

  const handleDirectCreation = () => {
    navigate('/backtest/create');
  };

  const handleBacktestClick = (backtest: any) => {
    navigate(`/backtest/results/${backtest.id}`);
  };

  const handleBacktestDelete = (backtest: any, index: number) => {
    const confirmDelete = window.confirm(`"${backtest.name}" 백테스트를 정말로 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`);
    if (confirmDelete) {
      // 배열에서 해당 항목 제거
      const newData = backtestData.filter((_, i) => i !== index);
      setBacktestData(newData);
      
      // 현재 페이지 조정 (마지막 페이지에서 모든 항목이 삭제된 경우)
      const newTotalPages = Math.ceil(newData.length / itemsPerPage);
      if (currentPage > newTotalPages && newTotalPages > 0) {
        setCurrentPage(newTotalPages);
      }
    }
  };


  return (
    <div className={styles.backtest}>
      <div className={styles.container}>
        {/* 백테스트 검색/생성 섹션 */}
        <BacktestSearchWidget
          onPortfolioModalOpen={handlePortfolioModalOpen}
          onDirectCreation={handleDirectCreation}
        />

        {/* 백테스트 히스토리 */}
        <BacktestHistoryWidget
          data={backtestData}
          onBacktestClick={handleBacktestClick}
          onBacktestDelete={handleBacktestDelete}
          currentPage={currentPage}
          itemsPerPage={itemsPerPage}
        />

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
        portfolios={portfolios}
      />
    </div>
  );
}