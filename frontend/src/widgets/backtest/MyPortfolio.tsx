import { motion, AnimatePresence } from 'motion/react';
import { Trash2, TrendingUp, AlertTriangle } from 'lucide-react';
import { useState, useEffect } from 'react';
import { createPortal } from 'react-dom';
import styles from './MyPortfolio.module.css';

interface PortfolioItem {
  name: string;
  code: string;
  buyPrice: string;
  quantity: number;
  targetWeight: number;
  threshold: number;
}

interface MyPortfolioProps {
  portfolioItems: PortfolioItem[];
  setPortfolioItems: (items: PortfolioItem[]) => void;
  selectedPortfolio: string;
  totalValue: number;
  onRemoveFromPortfolio: (code: string) => void;
  parsePrice: (priceString: string) => number;
  formatPrice: (price: number) => string;
  calculateValue: (buyPrice: string, quantity: number) => number;
  rebalancingType?: 'THRESHOLD' | 'PERIODIC';
}

export default function MyPortfolio({
  portfolioItems,
  setPortfolioItems,
  selectedPortfolio,
  totalValue,
  onRemoveFromPortfolio,
  parsePrice,
  formatPrice,
  calculateValue,
  rebalancingType = 'THRESHOLD'
}: MyPortfolioProps) {

  // 억 단위 이상 간소화 포맷팅
  const formatCompactPrice = (price: number): string => {
    if (price >= 100000000) { // 1억 이상
      const eok = price / 100000000;
      return `${eok.toFixed(1)}억원`;
    }
    return formatPrice(price);
  };
  const [warningVisible, setWarningVisible] = useState<{ [key: string]: boolean }>({});
  const [tooltipPosition, setTooltipPosition] = useState<{ [key: string]: { x: number; y: number } }>({});
  const [portalContainer, setPortalContainer] = useState<HTMLElement | null>(null);

  useEffect(() => {
    setPortalContainer(document.body);
  }, []);

  // 정규화된 비중 계산 함수
  const calculateNormalizedWeight = (item: PortfolioItem) => {
    const totalTargetWeight = portfolioItems.reduce((sum, portfolioItem) => sum + portfolioItem.targetWeight, 0);
    return totalTargetWeight > 0 ? (item.targetWeight / totalTargetWeight) * 100 : 0;
  };

  const checkRebalanceWarning = (item: PortfolioItem) => {
    if (totalValue === 0 || item.threshold === 0 || item.targetWeight === 0) return false;

    // 현재 평가액 기준 실제 비중 계산
    const currentValue = calculateValue(item.buyPrice, item.quantity);
    const actualWeight = (currentValue / totalValue) * 100;

    // 정규화된 목표비중 계산
    const normalizedTargetWeight = calculateNormalizedWeight(item);

    // 정규화된 목표비중 대비 편차율 계산: |실제비중 - 정규화목표비중| / 정규화목표비중 * 100
    const deviationRate = normalizedTargetWeight > 0 ? Math.abs(actualWeight - normalizedTargetWeight) / normalizedTargetWeight * 100 : 0;

    // 편차율이 임계값보다 큰 경우 경고
    return deviationRate > item.threshold;
  };

  const handleThresholdChange = (index: number, e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    const itemKey = `${portfolioItems[index].code}-${index}`;

    // 빈 문자열인 경우
    if (value === '') {
      const newItems = [...portfolioItems];
      newItems[index].threshold = 0;
      setPortfolioItems(newItems);
      setWarningVisible(prev => ({ ...prev, [itemKey]: false }));
      return;
    }

    // 숫자가 아닌 문자가 포함된 경우 입력을 막음
    if (!/^\d+$/.test(value)) {
      e.preventDefault();
      return false;
    }

    const numValue = parseInt(value, 10);
    if (numValue < 0 || numValue > 100) {
      return false;
    }

    const newItems = [...portfolioItems];
    newItems[index].threshold = numValue;
    setPortfolioItems(newItems);
  };

  const handleKeyPress = (e: React.KeyboardEvent<HTMLInputElement>) => {
    // 숫자, 백스페이스, Delete, Tab, Enter, Arrow keys만 허용
    if (!/[0-9]/.test(e.key) && 
        !['Backspace', 'Delete', 'Tab', 'Enter', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'].includes(e.key)) {
      e.preventDefault();
    }
  };
  return (
    <>
    {/* Portal for tooltips */}
    {portalContainer && Object.entries(warningVisible).map(([code, visible]) =>
      visible && tooltipPosition[code] && createPortal(
        <div
          key={code}
          className={styles.customTooltip}
          style={{
            left: tooltipPosition[code].x,
            top: tooltipPosition[code].y
          }}
        >
          <AlertTriangle className={styles.warningIcon} />
          <span>목표비중 대비 편차가 임계값을 초과합니다. 리밸런싱 시 대량 거래가 발생할 수 있습니다.</span>
        </div>,
        portalContainer
      )
    )}
    <motion.div
      initial={{ opacity: 0, x: 20 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.5, delay: 0.3 }}
      className={styles.portfolioCard}
    >
      <div className={styles.cardHeader}>
        <h3>나의 포트폴리오</h3>
        {selectedPortfolio && (
          <span className={styles.selectedPortfolio}>
            {selectedPortfolio}
          </span>
        )}
      </div>

      {portfolioItems.length > 0 ? (
        <>
          <div className={styles.portfolioTable}>
            <div className={styles.tableHeader} style={{
              gridTemplateColumns: rebalancingType === 'PERIODIC' ? '2fr 1fr 1fr 1fr 1fr 60px' : '2fr 1fr 1fr 1fr 1fr 1fr 60px'
            }}>
              <div className={styles.headerCell}>종목</div>
              <div className={styles.headerCell}>매수가</div>
              <div className={styles.headerCell}>수량</div>
              <div className={styles.headerCell}>비중</div>
              <div className={styles.headerCell}>평가금액</div>
              {rebalancingType === 'THRESHOLD' && <div className={styles.headerCell}>임계값</div>}
              <div className={styles.headerCell}>삭제</div>
            </div>

            <AnimatePresence initial={false}>
              {portfolioItems.map((item, index) => (
                <motion.div
                  key={item.code}
                  initial={{ opacity: 0, scaleY: 0 }}
                  animate={{ opacity: 1, scaleY: 1 }}
                  exit={{ opacity: 0, scaleY: 0 }}
                  transition={{ 
                    duration: 0.3,
                    ease: [0.25, 0.46, 0.45, 0.94]
                  }}
                  className={styles.tableRow}
                  style={{
                    transformOrigin: "top",
                    overflow: "hidden",
                    gridTemplateColumns: rebalancingType === 'PERIODIC' ? '2fr 1fr 1fr 1fr 1fr 60px' : '2fr 1fr 1fr 1fr 1fr 1fr 60px'
                  }}
                >
                <div className={styles.tableCell}>
                  <div className={styles.stockInfo}>
                    <span className={styles.stockName}>{item.name}</span>
                    <span className={styles.stockCode}>{item.code}</span>
                  </div>
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.priceText}>{item.buyPrice}</span>
                </div>
                <div className={styles.tableCell}>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => {
                      const value = parseInt(e.target.value) || 0;
                      const limitedValue = Math.min(Math.max(value, 1), 9999); // 1~9999 사이로 제한
                      const newItems = [...portfolioItems];
                      newItems[index].quantity = limitedValue;
                      setPortfolioItems(newItems);
                    }}
                    className={styles.numberInput}
                    min="1"
                    max="9999"
                  />
                  <span className={styles.unit}>주</span>
                </div>
                <div className={styles.tableCell}>
                  <div className={styles.weightContainer}>
                    <input
                      type="number"
                      value={item.targetWeight}
                      onChange={(e) => {
                        const newItems = [...portfolioItems];
                        newItems[index].targetWeight = parseInt(e.target.value) || 0;
                        setPortfolioItems(newItems);
                      }}
                      className={styles.numberInput}
                      min="0"
                      max="100"
                    />
                    <span className={styles.normalizedWeight}>
                      → {calculateNormalizedWeight(item).toFixed(1)}%
                    </span>
                  </div>
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.valueText}>
                    {formatCompactPrice(calculateValue(item.buyPrice, item.quantity))}
                  </span>
                </div>
                {rebalancingType === 'THRESHOLD' && (
                  <div className={styles.tableCell}>
                    <div className={styles.thresholdContainer}>
                      <input
                        type="text"
                        value={item.threshold === 0 ? '' : item.threshold}
                        onChange={(e) => handleThresholdChange(index, e)}
                        onKeyPress={handleKeyPress}
                        className={styles.numberInput}
                        placeholder="0"
                        maxLength={3}
                      />
                      <span className={styles.unit}>%</span>
                      {checkRebalanceWarning(item) && (
                        <div className={styles.warningIconContainer}>
                          <AlertTriangle
                            className={styles.permanentWarningIcon}
                            style={{
                              color: '#ff6b6b',
                              marginLeft: '5px',
                              width: '16px',
                              height: '16px'
                            }}
                            onMouseEnter={(e) => {
                              const rect = e.currentTarget.getBoundingClientRect();
                              setTooltipPosition(prev => ({
                                ...prev,
                                [item.code]: { x: rect.left, y: rect.top }
                              }));
                              setWarningVisible(prev => ({
                                ...prev,
                                [item.code]: true
                              }));
                            }}
                            onMouseLeave={() => setWarningVisible(prev => ({
                              ...prev,
                              [item.code]: false
                            }))}
                          />
                        </div>
                      )}
                    </div>
                  </div>
                )}
                <div className={styles.tableCell}>
                  <button
                    className={styles.deleteButton}
                    onClick={() => onRemoveFromPortfolio(item.code)}
                    title={`${item.name} 삭제`}
                  >
                    <Trash2 className={styles.deleteIcon} />
                  </button>
                </div>
              </motion.div>
              ))}
            </AnimatePresence>
          </div>

          <div className={styles.portfolioSummary}>
            <div className={styles.summaryItem}>
              <span>총 평가금액:</span>
              <span className={styles.summaryValue}>
                {formatCompactPrice(totalValue)}
              </span>
            </div>
            <div className={styles.summaryItem}>
              <span>총 종목 수:</span>
              <span className={styles.summaryValue}>
                {portfolioItems.length}개
              </span>
            </div>
          </div>
        </>
      ) : (
        <div className={styles.emptyState}>
          <TrendingUp className={styles.emptyIcon} />
          <h4>포트폴리오를 구성해보세요</h4>
          <p>왼쪽에서 종목을 검색하여 추가하세요</p>
        </div>
      )}
    </motion.div>
    </>
  );
}