import { useState, useMemo } from 'react';
import styles from './HoldingsTableWidget.module.css';
import { stockListData } from './stockListData';
import HoldingsPagination from './components/HoldingsPagination';
import TableLayoutContainer from './components/TableLayoutContainer';

interface HoldingsTableWidgetProps {
  onStockSelect: (stock: { code: string; name: string }) => void;
}

export default function HoldingsTableWidget_v2({ onStockSelect }: HoldingsTableWidgetProps) {
  const [currentPage, setCurrentPage] = useState(1);
  const [hoveredRowIndex, setHoveredRowIndex] = useState<number | null>(null);

  const itemsPerPage = 5; // 10개에서 5개로 변경
  const totalPages = Math.ceil(stockListData.length / itemsPerPage);

  const currentPageData = useMemo(() => {
    const startIndex = (currentPage - 1) * itemsPerPage;
    return stockListData.slice(startIndex, startIndex + itemsPerPage);
  }, [currentPage, itemsPerPage]);

  const formatNumber = (num: number) => {
    return new Intl.NumberFormat('ko-KR').format(num);
  };

  const handlePageChange = (page: number) => {
    setHoveredRowIndex(null); // Reset hover when changing pages
    setCurrentPage(page);
  };

  const controls = (
    <div className={styles.paginationContainer}>
      <HoldingsPagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
    </div>
  );

  const table = (
    <div className={styles.holdingTable}>
      <div className={styles.tableContent}>
        {/* 종목명 (단일 컬럼) */}
        <div className={`${styles.tableColumn} ${styles.singleColumn}`}>
          <div className={styles.singleColumnHeader}>종목명</div>
          <div className={styles.columnData}>
            {currentPageData.map((stock, index) => (
              <div
                key={stock.rank}
                className={`${styles.singleDataValue} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                {stock.name}
              </div>
            ))}
          </div>
        </div>

        {/* 매입가/현재가 */}
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>매입가</div>
          <div className={styles.columnHeaderSecond}>현재가</div>
          <div className={styles.columnData}>
            {currentPageData.map((stock, index) => (
              <div
                key={`price-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={styles.dataValue}>{formatNumber(stock.buyPrice)}원</div>
                <div className={styles.dataValue}>{formatNumber(stock.price)}원</div>
              </div>
            ))}
          </div>
        </div>

        {/* 평가손익/수익률 */}
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>평가손익</div>
          <div className={styles.columnHeaderSecond}>수익률</div>
          <div className={styles.columnData}>
            {currentPageData.map((stock, index) => (
              <div
                key={`profit-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={`${styles.dataValue} ${stock.profitLoss >= 0 ? styles.profitValue : styles.lossValue}`}>
                  {stock.profitLoss >= 0 ? '+' : ''}
                  {formatNumber(stock.profitLoss)}
                </div>
                <div className={`${styles.dataValue} ${stock.profitRate >= 0 ? styles.profitValue : styles.lossValue}`}>
                  {stock.profitRate >= 0 ? '+' : ''}
                  {stock.profitRate}%
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
            {currentPageData.map((stock, index) => (
              <div
                key={`change-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={`${styles.dataValue} ${stock.comparison >= 0 ? styles.profitValue : styles.lossValue}`}>
                  {stock.comparison >= 0 ? '+' : ''}
                  {formatNumber(stock.comparison)}
                </div>
                <div className={`${styles.dataValue} ${stock.changePercent ? styles.profitValue : styles.lossValue}`}>
                  {stock.changePercent ? '+' : ''}
                  {stock.change.toFixed(1)}%
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
            {currentPageData.map((stock, index) => (
              <div
                key={`amount-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={styles.dataValue}>{formatNumber(stock.buyAmount)}</div>
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
            {currentPageData.map((stock, index) => (
              <div
                key={`quantity-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={styles.dataValue}>{stock.holdingQuantity}</div>
                <div className={styles.dataValue}>{stock.availableQuantity}</div>
              </div>
            ))}
          </div>
        </div>

        {/* 수수료/세금 */}
        <div className={styles.tableColumn}>
          <div className={styles.columnHeader}>수수료</div>
          <div className={styles.columnHeaderSecond}>세금</div>
          <div className={styles.columnData}>
            {currentPageData.map((stock, index) => (
              <div
                key={`fee-${stock.rank}`}
                className={`${styles.dataRow} ${index % 2 === 0 ? styles.evenRow : ''} ${
                  hoveredRowIndex === index ? styles.hovered : ''
                }`}
                onClick={() => onStockSelect({ code: stock.code, name: stock.name })}
                onMouseEnter={() => setHoveredRowIndex(index)}
                onMouseLeave={() => setHoveredRowIndex(null)}
              >
                <div className={styles.dataValue}>{stock.fee}</div>
                <div className={styles.dataValue}>{stock.tax}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );

  return <TableLayoutContainer controls={controls} table={table} />;
}
