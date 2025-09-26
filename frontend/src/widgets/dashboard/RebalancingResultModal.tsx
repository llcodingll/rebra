import BaseModal from '../../shared/ui/modal/BaseModal';
import styles from './RebalancingResultModal.module.css';

interface OrderResult {
  stockCode: string;
  stockName: string | null;
  orderType: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  success: boolean;
  orderId: string;
  errorMessage: string | null;
}

interface RebalancingResult {
  success: boolean;
  rebalancingOrderId: string | null;
  executionTime: string;
  totalBuyAmount: number;
  totalSellAmount: number;
  totalPortfolioValue: number | null;
  orderResults: OrderResult[];
  failureReason: string | null;
}

interface RebalancingResultModalProps {
  isOpen: boolean;
  onClose: () => void;
  result: RebalancingResult;
  portfolioStocks?: Array<{
    stockCode: string;
    stockName: string;
  }>;
}

export default function RebalancingResultModal({
  isOpen,
  onClose,
  result,
  portfolioStocks = []
}: RebalancingResultModalProps) {
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('ko-KR').format(price);
  };

  const formatDateTime = (dateTime: string) => {
    return new Date(dateTime).toLocaleString('ko-KR');
  };

  // 리밸런싱 결과 데이터 로그 출력
  console.log('🔄 리밸런싱 결과 데이터:', result);
  console.log('📊 주문 결과들:', result.orderResults);
  console.log('🏦 포트폴리오 주식 데이터:', portfolioStocks);

  // stockCode로 stockName을 찾는 함수
  const getStockNameByCode = (stockCode: string): string => {
    const stock = portfolioStocks.find(stock => stock.stockCode === stockCode);
    return stock?.stockName || stockCode;
  };

  const successfulOrders = result.orderResults?.filter(order => order.success) || [];
  const failedOrders = result.orderResults?.filter(order => !order.success) || [];

  return (
    <BaseModal
      isOpen={isOpen}
      onClose={onClose}
      title="리밸런싱 결과"
      size="large"
    >
      <div className={styles.content}>
        {/* 전체 결과 요약 */}
        <div className={styles.summarySection}>
          <div className={styles.statusBadge}>
            <span className={`${styles.badge} ${result.success ? styles.success : styles.error}`}>
              {result.success ? '성공' : '실패'}
            </span>
          </div>

          {result.failureReason === "리밸런싱할 주문이 없습니다" ? (
            <div className={styles.noRebalancingMessage}>
              <h3>목표 비중에 대해 최적의 포트폴리오 상태입니다</h3>
              <p>리밸런싱이 필요하지 않아 거래가 수행되지 않았습니다.</p>
            </div>
          ) : (
            <div className={styles.summaryGrid}>
              <div className={styles.summaryItem}>
                <span className={styles.label}>실행 시간</span>
                <span className={styles.value}>{formatDateTime(result.executionTime)}</span>
              </div>
              <div className={styles.summaryItem}>
                <span className={styles.label}>총 매수 금액</span>
                <span className={styles.value}>{formatPrice(result.totalBuyAmount)}원</span>
              </div>
              <div className={styles.summaryItem}>
                <span className={styles.label}>총 매도 금액</span>
                <span className={styles.value}>{formatPrice(result.totalSellAmount)}원</span>
              </div>
              <div className={styles.summaryItem}>
                <span className={styles.label}>처리된 주문</span>
                <span className={styles.value}>
                  성공 {successfulOrders.length}건 / 실패 {failedOrders.length}건
                </span>
              </div>
            </div>
          )}
        </div>

        {/* 성공한 거래 내역 */}
        {successfulOrders.length > 0 && (
          <div className={styles.orderSection}>
            <h3 className={styles.sectionTitle}>완료된 거래</h3>
            <div className={styles.orderList}>
              {successfulOrders.map((order, index) => (
                <div key={index} className={styles.orderItem}>
                  <div className={styles.orderHeader}>
                    <span className={`${styles.orderType} ${styles[order.orderType.toLowerCase()]}`}>
                      {order.orderType === 'BUY' ? '매수' : '매도'}
                    </span>
                    <span className={styles.stockCode}>{getStockNameByCode(order.stockCode)} ({order.stockCode})</span>
                    <span className={styles.orderId}>#{order.orderId}</span>
                  </div>
                  <div className={styles.orderDetails}>
                    <span>수량: {formatPrice(order.quantity)}주</span>
                    <span>가격: {formatPrice(order.price)}원</span>
                    <span>총액: {formatPrice(order.quantity * order.price)}원</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 실패한 거래 내역 */}
        {failedOrders.length > 0 && (
          <div className={styles.orderSection}>
            <h3 className={styles.sectionTitle}>실패한 거래</h3>
            <div className={styles.orderList}>
              {failedOrders.map((order, index) => (
                <div key={index} className={`${styles.orderItem} ${styles.failed}`}>
                  <div className={styles.orderHeader}>
                    <span className={`${styles.orderType} ${styles.failed}`}>
                      {order.orderType === 'BUY' ? '매수' : '매도'}
                    </span>
                    <span className={styles.stockCode}>{getStockNameByCode(order.stockCode)} ({order.stockCode})</span>
                  </div>
                  <div className={styles.orderDetails}>
                    <span>수량: {formatPrice(order.quantity)}주</span>
                    <span>가격: {formatPrice(order.price)}원</span>
                    {order.errorMessage && (
                      <span className={styles.errorMessage}>{order.errorMessage}</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 전체 실패 사유 */}
        {result.failureReason && (
          <div className={styles.errorSection}>
            <h3 className={styles.sectionTitle}>실패 사유</h3>
            <p className={styles.errorText}>{result.failureReason}</p>
          </div>
        )}

        {/* 확인 버튼 */}
        <div className={styles.buttonGroup}>
          <button className={styles.confirmButton} onClick={onClose}>
            확인
          </button>
        </div>
      </div>
    </BaseModal>
  );
}