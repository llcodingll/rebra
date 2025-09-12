import { motion, AnimatePresence } from 'motion/react';
import { Trash2, TrendingUp, AlertTriangle } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
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
}

export default function MyPortfolio({
  portfolioItems,
  setPortfolioItems,
  selectedPortfolio,
  totalValue,
  onRemoveFromPortfolio,
  parsePrice,
  formatPrice,
  calculateValue
}: MyPortfolioProps) {
  const [warningVisible, setWarningVisible] = useState<{ [key: string]: boolean }>({});
  const [tooltipPosition, setTooltipPosition] = useState<{ [key: string]: { top: number, left: number } }>({});
  const inputRefs = useRef<{ [key: string]: HTMLInputElement | null }>({});

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

    // 유효한 숫자 입력 후에만 경고창 표시
    if (numValue <= 20 && numValue > 0) {
      const inputElement = inputRefs.current[itemKey];
      if (inputElement) {
        const rect = inputElement.getBoundingClientRect();
        setTooltipPosition(prev => ({
          ...prev,
          [itemKey]: {
            top: rect.top - 60,
            left: rect.left + rect.width / 2
          }
        }));
      }
      setWarningVisible(prev => ({ ...prev, [itemKey]: true }));
      setTimeout(() => {
        setWarningVisible(prev => ({ ...prev, [itemKey]: false }));
      }, 3000);
    } else {
      setWarningVisible(prev => ({ ...prev, [itemKey]: false }));
    }
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
            <div className={styles.tableHeader}>
              <div className={styles.headerCell}>종목</div>
              <div className={styles.headerCell}>매수가</div>
              <div className={styles.headerCell}>수량</div>
              <div className={styles.headerCell}>가중치</div>
              <div className={styles.headerCell}>평가금액</div>
              <div className={styles.headerCell}>임계값</div>
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
                    overflow: "hidden"
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
                      const newItems = [...portfolioItems];
                      newItems[index].quantity = parseInt(e.target.value) || 0;
                      setPortfolioItems(newItems);
                    }}
                    className={styles.numberInput}
                    min="1"
                  />
                  <span className={styles.unit}>주</span>
                </div>
                <div className={styles.tableCell}>
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
                </div>
                <div className={styles.tableCell}>
                  <span className={styles.valueText}>
                    {formatPrice(calculateValue(item.buyPrice, item.quantity))}
                  </span>
                </div>
                <div className={styles.tableCell}>
                  <div className={styles.thresholdContainer}>
                    <input
                      ref={(el) => { inputRefs.current[`${item.code}-${index}`] = el; }}
                      type="text"
                      value={item.threshold === 0 ? '' : item.threshold}
                      onChange={(e) => handleThresholdChange(index, e)}
                      onKeyPress={handleKeyPress}
                      className={styles.numberInput}
                      placeholder="0"
                      maxLength={3}
                    />
                    <span className={styles.unit}>%</span>
                  </div>
                </div>
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
                {totalValue.toLocaleString()}원
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
          <h4>포트폴리오가 비어있습니다</h4>
          <p>왼쪽에서 종목을 검색하여 포트폴리오에 추가해보세요</p>
        </div>
      )}
    </motion.div>
    
    {/* Portal로 툴팁을 body에 렌더링 */}
    {Object.entries(warningVisible).map(([key, visible]) => {
      if (!visible || !tooltipPosition[key]) return null;
      
      return createPortal(
        <div 
          key={key}
          className={styles.warningTooltip}
          style={{
            position: 'fixed',
            top: tooltipPosition[key].top,
            left: tooltipPosition[key].left,
            transform: 'translateX(-50%)',
            zIndex: 999999
          }}
        >
          <AlertTriangle className={styles.warningIcon} />
          <span>임계값이 낮으면 잦은 거래로 인한 수수료 부담이 클 수 있습니다</span>
        </div>,
        document.body
      );
    })}
    </>
  );
}