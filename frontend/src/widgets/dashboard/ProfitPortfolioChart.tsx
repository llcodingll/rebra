import { motion } from 'motion/react';
import styles from './ProfitPortfolioChart.module.css';
import { TrendingUp, TrendingDown, DollarSign } from 'lucide-react';
import DashboardChart from './CumulativeReturnsChart';
import { useState } from 'react';
import { useApi } from '../../shared/hook/useApi';
import { portfolioApi } from '../../features/portfolio/api/portfolioApi';
import { transformRebalancingHistoryToChart, transformRebalancingHistoryToPercents } from '../../features/portfolio/utils/portfolioTransform';
import HistoryPagination from './HistoryPagination';

import type { Stock } from '../../entities/portfolio';

interface ProfitPortfolioChartProps {
  data: Stock[];
  portfolioId?: number;
}


export default function ProfitPortfolioChart({ data, portfolioId }: ProfitPortfolioChartProps) {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  // 리밸런싱 히스토리 API 호출 (그래프용)
  const { data: chartHistoryData, isLoading: isHistoryLoading, error: historyError } = useApi({
    queryKey: ['rebalancing-history', portfolioId],
    apiFunction: () => portfolioId ? portfolioApi.getRebalancingHistory(portfolioId) : Promise.reject('No portfolio ID'),
    enabled: !!portfolioId,
  });

  // 리밸런싱 히스토리 테이블 API 호출
  const { data: tableHistoryData, isLoading: isTableLoading, error: tableError } = useApi({
    queryKey: ['rebalancing-history-table', portfolioId, currentPage, itemsPerPage],
    apiFunction: () => portfolioId ? portfolioApi.getRebalancingHistoryTable(portfolioId, currentPage - 1, itemsPerPage) : Promise.reject('No portfolio ID'),
    enabled: !!portfolioId,
  });

  // 선택된 리밸런싱 상세 조회 API 호출
  const { data: selectedHistoryDetail, isLoading: isDetailLoading, error: detailError } = useApi({
    queryKey: ['rebalancing-history-detail', portfolioId, selectedId],
    apiFunction: () => portfolioId && selectedId ? portfolioApi.getRebalancingHistoryDetail(portfolioId, selectedId) : Promise.reject('No portfolio ID or selected ID'),
    enabled: !!portfolioId && !!selectedId,
  });

    // API 데이터 확인용 로깅
  console.log('===  선택된 리밸런싱 상세 조회 API 데이터 ===' , selectedHistoryDetail);


  // API 데이터 확인용 로깅
  console.log('=== ProfitPortfolioChart API 데이터 ===');
  console.log('portfolioId:', portfolioId);
  console.log('isHistoryLoading:', isHistoryLoading);
  console.log('historyError:', historyError);
  console.log('chartHistoryData:', chartHistoryData);
  console.log('chartHistoryData?.data:', chartHistoryData?.data);
  console.log('실제 사용할 데이터:', chartHistoryData?.data || chartHistoryData);

  // 테이블 데이터 및 페이지네이션 정보 (API 또는 mock 데이터)
  const historyTableItems = tableHistoryData?.content?.histories || [];
  const pageInfo = tableHistoryData?.pageInfo;
  const totalPages = pageInfo?.totalPages || 0;

  console.log('=== 테이블 API 데이터 ===');
  console.log('tableHistoryData:', tableHistoryData);
  console.log('historyTableItems:', historyTableItems);
  console.log('pageInfo:', pageInfo);
  console.log('totalPages:', totalPages);

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 등록된 주식이 없는 경우 빈 상태 표시
  if (data.length === 0) {
    return (
      <div className={styles.chartContainer}>
        <motion.div 
          initial={{ opacity: 1, y: 0 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className={styles.chartCard}
        >
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <DollarSign className={styles.headerIcon} />
              <h2 className={styles.title}>히스토리</h2>
            </div>
          </div>
          
          <div className={styles.emptyState}>
            <motion.div 
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.emptyStateContent}
            >
              <TrendingUp className={styles.emptyStateIcon} />
              <h3 className={styles.emptyStateTitle}>포트폴리오에 주식을 등록해보세요!</h3>
              <p className={styles.emptyStateDescription}>
                주식을 등록하면 수익률과 리밸런싱 분석을 확인할 수 있습니다.
              </p>
            </motion.div>
          </div>
        </motion.div>
      </div>
    );
  }

  // 총 평가액 계산
  const totalValue = data.reduce((sum, stock) => {
    return sum + stock.totalValue;
  }, 0);

  // 총 수익 계산
  const totalReturn = data.reduce((sum, stock) => {
    return sum + stock.profitLoss;
  }, 0);

  const totalReturnPercent = ((totalReturn / (totalValue - totalReturn)) * 100).toFixed(1);
  const isPositiveReturn = totalReturn >= 0;

  // 현재 선택된 ID의 거래 내역 가져오기 (API 데이터 사용)
  const getCurrentTrades = () => {
    if (!selectedHistoryDetail?.trades) return [];
    console.log('API 거래 내역:', selectedHistoryDetail.trades);
    return selectedHistoryDetail.trades;
  };

  // API 데이터가 로딩 중이거나 없는 경우 처리
  if (isHistoryLoading) {
    return (
      <div className={styles.chartContainer}>
        <div className={styles.chartCard}>
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <DollarSign className={styles.headerIcon} />
              <h2 className={styles.title}>히스토리</h2>
            </div>
          </div>
          <div className={styles.content}>
            <div>리밸런싱 히스토리를 불러오는 중...</div>
          </div>
        </div>
      </div>
    );
  }

  if (historyError) {
    return (
      <div className={styles.chartContainer}>
        <div className={styles.chartCard}>
          <div className={styles.header}>
            <div className={styles.headerLeft}>
              <DollarSign className={styles.headerIcon} />
              <h2 className={styles.title}>히스토리</h2>
            </div>
          </div>
          <div className={styles.content}>
            <div>리밸런싱 히스토리를 불러오는데 실패했습니다.</div>
          </div>
        </div>
      </div>
    );
  }

  // 실제 히스토리 데이터 추출 (API 응답 구조에 따라)
  const actualHistoryData = chartHistoryData?.data || chartHistoryData || [];
  console.log('실제 히스토리 데이터:', actualHistoryData);

  // 차트 데이터 변환 (transform 함수 사용)
  const chartData = transformRebalancingHistoryToChart(actualHistoryData);

  // 성과 데이터 계산
  const portfolioPercents = transformRebalancingHistoryToPercents(actualHistoryData);
  const kospiValues = actualHistoryData?.map((_, i) => 100 + i * 1.8) || [];
  const kospiPercents = kospiValues;

  // 차트 스케일 계산
  const allValues = [...portfolioPercents, ...kospiPercents];
  const minValue = Math.min(...allValues);
  const maxValue = Math.max(...allValues);
  const valueRange = maxValue - minValue;
  const padding = valueRange * 0.1;
  const chartMin = Math.max(minValue - padding, 95);
  const chartMax = maxValue + padding;
  const chartRange = chartMax - chartMin;

  // Y축 라벨 생성
  const yAxisLabels: string[] = [];
  for (let i = 0; i <= 4; i++) {
    const value = chartMin + (chartRange / 4) * i;
    yAxisLabels.unshift(value.toFixed(1) + '%');
  }

  // SVG 경로 생성
  const createPath = (values: number[]) => {
    const width = 800;
    const height = 400;
    const padding = 40;

    return values
      .map((value, index) => {
        const x = (index / (values.length - 1)) * (width - 2 * padding) + padding;
        const y = height - padding - ((value - chartMin) / chartRange) * (height - 2 * padding);
        return `${index === 0 ? 'M' : 'L'} ${x} ${y}`;
      })
      .join(' ');
  };

  return (
    <div className={styles.chartContainer}>
      <div className={styles.chartCard}>
        {/* 헤더 */}
        <div className={styles.header}>
          <div className={styles.headerLeft}>
            <DollarSign className={styles.headerIcon} />
            <h2 className={styles.title}>히스토리</h2>
          </div>
          <div className={styles.headerBadge}>
            <span>총 {data.length}개 종목</span>
          </div>
        </div>

        {/* 메인 컨텐츠 */}
        <div className={styles.content}>
          {/* 좌측 컬럼 */}
          <div className={styles.leftColumn}>
            {/* 총 평가액 정보 */}
            <motion.div
              initial={{ opacity: 0, x: -20 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
              className={styles.totalValueSection}
            >
              <div className={styles.totalValueHeader}>
                <h3>총 평가액</h3>
                <div className={styles.returnIcon}>
                  {isPositiveReturn ? (
                    <TrendingUp className={styles.iconPositive} />
                  ) : (
                    <TrendingDown className={styles.iconNegative} />
                  )}
                </div>
              </div>
              <div className={styles.amount}>{totalValue.toLocaleString()}원</div>
              <div className={`${styles.returnInfo} ${isPositiveReturn ? styles.positive : styles.negative}`}>
                <span className={styles.returnAmount}>
                  {totalReturn >= 0 ? '+' : ''}{totalReturn.toLocaleString()}원
                </span>
                <span className={styles.returnPercent}>
                  ({totalReturn >= 0 ? '+' : ''}{totalReturnPercent}%)
                </span>
              </div>
            </motion.div>

            {/* 거래 내역 */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.3 }}
              className={styles.recentTrades}
            >
              <h3 className={styles.tradesTitle}>
                {selectedId ? `거래 내역 (ID: ${selectedId})` : '거래 내역을 보려면 차트나 히스토리를 클릭하세요'}
              </h3>
              {isDetailLoading ? (
                <div className={styles.noTrades}>거래 내역을 불러오는 중...</div>
              ) : getCurrentTrades().length > 0 ? (
                <div className={styles.tradesScrollContainer}>
                  {getCurrentTrades().map((trade) => (
                    <div key={trade.tradeId} className={`${styles.tradeItem} ${trade.tradeType === 'BUY' ? styles.buyTrade : styles.sellTrade}`}>
                      <div className={styles.tradeHeader}>
                        <div className={styles.stockInfo}>
                          <span className={styles.stockName}>{trade.stockName}</span>
                          <span className={`${styles.tradeTypeBadge} ${trade.tradeType === 'BUY' ? styles.buyBadge : styles.sellBadge}`}>
                            {trade.tradeType === 'BUY' ? '매수' : '매도'}
                          </span>
                        </div>
                        {trade.tradeType === 'SELL' && (
                          <div className={styles.tradeResult}>
                            <span className={trade.profitAmount >= 0 ? styles.profit : styles.loss}>
                              손익 {trade.profitAmount >= 0 ? '+' : ''}{trade.profitAmount.toLocaleString()}원
                            </span>
                            <span className={trade.profitRate >= 0 ? styles.profitRate : styles.lossRate}>
                              {trade.profitRate >= 0 ? '+' : ''}{trade.profitRate}%
                            </span>
                          </div>
                        )}
                      </div>
                      <span className={styles.tradeDetail}>
                        {trade.executedShares}주({(trade.executedShares * trade.price).toLocaleString()}원)
                      </span>
                      <div className={styles.tradeReason}>
                        {trade.reason}
                      </div>
                    </div>
                  ))}
                </div>
              ) : selectedId ? (
                <div className={styles.noTrades}>해당 리밸런싱에 거래 내역이 없습니다.</div>
              ) : (
                <div className={styles.noTrades}>차트의 점이나 히스토리 행을 클릭해서 거래 내역을 확인하세요.</div>
              )}
            </motion.div>
          </div>

          {/* 차트 */}
          <motion.div 
            initial={{ opacity: 0, scale: 0.8 }}
            animate={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.4 }}
            className={styles.chartWrapper}
          >
            <DashboardChart
              portfolioId={portfolioId}
            />
          </motion.div>

          {/* 리밸런싱 히스토리 */}
          <motion.div 
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
            className={styles.historyContainer}
          >
            <div className={styles.historyHeader}>
              <h3>리밸런싱 히스토리</h3>
            </div>
            
            <div className={styles.historyTable}>
              <div className={styles.historyTableHead}>
                <div className={styles.historyColumnHeader}>실행 일시</div>
                <div className={styles.historyColumnHeader}>유형</div>
                <div className={styles.historyColumnHeader}>거래 종목</div>
                <div className={styles.historyColumnHeader}>매수 금액</div>
                <div className={styles.historyColumnHeader}>매도 금액</div>
              </div>

              <div className={styles.historyTableBody}>
                {historyTableItems.map((item) => (
                  <div
                    key={item.orderId}
                    className={styles.historyRow}
                    onClick={() => {
                      console.log('History clicked ID:', item.orderId);
                      setSelectedId(item.orderId);
                    }}
                  >
                    <div className={styles.historyCell}>
                      {new Date(item.executedAt).toLocaleDateString('ko-KR')}
                    </div>
                    <div className={styles.historyCell}>
                      <span className={`${styles.typeBadge} ${item.executionType === 'AUTO' ? styles.auto : styles.manual}`}>
                        {item.executionType === 'AUTO' ? '자동' : '수동'}
                      </span>
                    </div>
                    <div className={styles.historyCell}>{item.totalStocks}개</div>
                    <div className={styles.historyCell}>
                      <span className={styles.buyAmount}>{item.totalBuyAmount.toLocaleString()}원</span>
                    </div>
                    <div className={styles.historyCell}>
                      <span className={styles.sellAmount}>{Math.abs(item.totalSellAmount).toLocaleString()}원</span>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* 페이지네이션 */}
            <HistoryPagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </motion.div>
        </div>
        </div>
    </div>
  );
}