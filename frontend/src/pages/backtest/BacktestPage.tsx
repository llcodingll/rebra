import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import styles from './BacktestPage.module.css';
import Pagination from '../../widgets/common/Pagination';
import BacktestSearchWidget from '../../widgets/backtest/BacktestSearchWidget';
import BacktestHistoryWidget from '../../widgets/backtest/BacktestHistoryWidget';
import { getBacktestList, deleteBacktest, type BacktestListResponse } from '../../features/backtest/api/backtestApi';

export default function BacktestPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // 백테스트 목록 조회
  const { data: backtestResponse, isLoading, error } = useQuery({
    queryKey: ['backtestList', currentPage - 1, itemsPerPage],
    queryFn: async () => {
      const result = await getBacktestList(currentPage - 1, itemsPerPage);
      if (result.success) {
        return result.data;
      } else {
        throw new Error(result.error.message);
      }
    },
    staleTime: 30000, // 30초
  });

  // 백테스트 삭제 mutation
  const deleteMutation = useMutation({
    mutationFn: deleteBacktest,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['backtestList'] });
    },
  });

  const backtestData = backtestResponse?.content || [];
  const totalPages = backtestResponse?.totalPages || 0;

  const handleDirectCreation = () => {
    navigate('/backtest/create');
  };

  const handleBacktestClick = (backtest: any) => {
    navigate(`/backtest/results/${backtest.id}`);
  };

  const handleBacktestDelete = (backtest: BacktestListResponse, index: number) => {
    const confirmDelete = window.confirm(`"${backtest.testName}" 백테스트를 정말로 삭제하시겠습니까?\n\n이 작업은 되돌릴 수 없습니다.`);
    if (confirmDelete) {
      deleteMutation.mutate(backtest.id);
    }
  };


  return (
    <div className={styles.backtest}>
      <div className={styles.container}>
        {/* 백테스트 검색/생성 섹션 */}
        <BacktestSearchWidget
          onDirectCreation={handleDirectCreation}
        />

        {/* 백테스트 히스토리 */}
        <BacktestHistoryWidget
          data={backtestData}
          onBacktestClick={handleBacktestClick}
          onBacktestDelete={handleBacktestDelete}
          currentPage={currentPage}
          itemsPerPage={itemsPerPage}
          isLoading={isLoading}
          error={error}
        />

        {!isLoading && (
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={setCurrentPage}
          />
        )}
      </div>

    </div>
  );
}