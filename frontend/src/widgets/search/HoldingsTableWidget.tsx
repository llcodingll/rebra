import { useState } from 'react';
import styles from './HoldingsTableWidget.module.css';
import HoldingsPagination from './components/HoldingsPagination';
import TableLayoutContainer from './components/TableLayoutContainer';
import { useHoldings } from '../../features/stock-search/hooks/useHoldings';

interface HoldingsTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function HoldingsTableWidget_v2({ onStockSelect }: HoldingsTableWidgetProps) {
  const [currentPage, setCurrentPage] = useState(0);
  const [hoveredRowIndex, setHoveredRowIndex] = useState<number | null>(null);

  const itemsPerPage = 5;
  const { holdings, isLoading, error, isEmpty, hasAccount } = useHoldings(currentPage, itemsPerPage);

  // 임시로 총 페이지 수 계산 (실제로는 API에서 받아와야 함)
  const totalPages = Math.ceil(holdings.length / itemsPerPage) || 1;

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const handlePageChange = (page: number) => {
    setHoveredRowIndex(null); // Reset hover when changing pages
    setCurrentPage(page);
  };

  const controls =
    totalPages > 1 ? (
      <div className={styles.paginationContainer}>
        <HoldingsPagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
      </div>
    ) : null;

  const table = (
    <div className={styles.holdingTable}>
      {/* 로딩 상태 */}
      {isLoading && <div className={styles.loadingMessage}>보유 종목을 불러오는 중...</div>}

      {/* 에러 상태 */}
      {error && <div className={styles.errorMessage}>데이터를 불러올 수 없습니다</div>}

      {/* 계정 연결 안됨 */}
      {!hasAccount && <div className={styles.emptyMessage}>계정을 먼저 연결해주세요</div>}

      {/* 보유 종목 없음 */}
      {hasAccount && !isLoading && !error && isEmpty && (
        <div className={styles.emptyMessage}>보유중인 종목이 없습니다 </div>
      )}

      {/* 데이터 표시 */}
      {hasAccount && !isLoading && !error && !isEmpty && (
        <div className={styles.tableContent}>
          {/* 종목명 (단일 컬럼) */}
          <div className={`${styles.tableColumn} ${styles.singleColumn}`}>
            <div className={styles.singleColumnHeader}>종목명</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={stock.stockCode}
                  className={`${styles.singleDataValue} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  {stock.stockName}
                </div>
              ))}
            </div>
          </div>

          {/* 매입가/현재가 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>매입가</div>
            <div className={styles.columnHeaderSecond}>현재가</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`price-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div className={styles.dataValue}>{formatNumber(stock.averagePurchasePrice)}원</div>
                  <div className={styles.dataValue}>{formatNumber(stock.currentPrice)}원</div>
                </div>
              ))}
            </div>
          </div>

          {/* 평가손익/수익률 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>평가손익</div>
            <div className={styles.columnHeaderSecond}>수익률</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`profit-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div
                    className={`${styles.dataValue} ${
                      stock.evaluationProfitLoss >= 0 ? styles.profitValue : styles.lossValue
                    }`}
                  >
                    {stock.evaluationProfitLoss >= 0 ? '+' : ''}
                    {formatNumber(stock.evaluationProfitLoss)}
                  </div>
                  <div
                    className={`${styles.dataValue} ${stock.returnRate >= 0 ? styles.profitValue : styles.lossValue}`}
                  >
                    {stock.returnRate >= 0 ? '+' : ''}
                    {stock.returnRate.toFixed(2)}%
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 대비/등락률 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>대비</div>
            <div className={styles.columnHeaderSecond}>등락률</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`change-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div
                    className={`${styles.dataValue} ${stock.priceChange >= 0 ? styles.profitValue : styles.lossValue}`}
                  >
                    {stock.priceChange >= 0 ? '+' : ''}
                    {formatNumber(stock.priceChange)}
                  </div>
                  <div
                    className={`${styles.dataValue} ${stock.changeRate >= 0 ? styles.profitValue : styles.lossValue}`}
                  >
                    {stock.changeRate >= 0 ? '+' : ''}
                    {stock.changeRate.toFixed(2)}%
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* 매입금액/평가금액 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>매입금액</div>
            <div className={styles.columnHeaderSecond}>평가금액</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`amount-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div className={styles.dataValue}>{formatNumber(stock.purchaseAmount)}</div>
                  <div className={styles.dataValue}>{formatNumber(stock.evaluationAmount)}</div>
                </div>
              ))}
            </div>
          </div>

          {/* 보유수량/가능수량 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>보유수량</div>
            <div className={styles.columnHeaderSecond}>가능수량</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`quantity-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div className={styles.dataValue}>{stock.holdingQuantity}</div>
                  <div className={styles.dataValue}>{stock.orderableQuantity}</div>
                </div>
              ))}
            </div>
          </div>

          {/* 수수료/세금 */}
          <div className={styles.tableColumn}>
            <div className={styles.columnHeader}>수수료</div>
            <div className={styles.columnHeaderSecond}>세금</div>
            <div className={styles.columnData}>
              {holdings.map((stock, index) => (
                <div
                  key={`fee-${stock.stockCode}`}
                  className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                    hoveredRowIndex === index ? styles.hovered : ''
                  }`}
                  onClick={() => onStockSelect({ code: stock.stockCode, name: stock.stockName })}
                  onMouseEnter={() => setHoveredRowIndex(index)}
                  onMouseLeave={() => setHoveredRowIndex(null)}
                >
                  <div className={styles.dataValue}>{formatNumber(stock.fee)}</div>
                  <div className={styles.dataValue}>{formatNumber(stock.tax)}</div>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
