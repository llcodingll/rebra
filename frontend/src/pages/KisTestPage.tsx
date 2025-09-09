import { useState } from 'react';
import { RealTimeChart } from '../widget/kis/RealTimeChart';
import styles from './KisTestPage.module.css';

export function KisTestPage() {
  const [credentials, setCredentials] = useState({
    appkey: '',
    appsecret: ''
  });
  const [stockCode, setStockCode] = useState('005930'); // 삼성전자 기본값
  const [stockName, setStockName] = useState('삼성전자');
  const [isConnected, setIsConnected] = useState(false);
  
  // 주요 종목 목록
  const popularStocks = [
    { code: '005930', name: '삼성전자' },
    { code: '000660', name: 'SK하이닉스' },
    { code: '035420', name: 'NAVER' },
    { code: '051910', name: 'LG화학' },
    { code: '006400', name: '삼성SDI' },
    { code: '028260', name: '삼성물산' },
    { code: '012330', name: '현대모비스' },
    { code: '207940', name: '삼성바이오로직스' },
  ];

  const handleConnect = () => {
    if (!credentials.appkey || !credentials.appsecret) {
      alert('App Key와 App Secret을 모두 입력해주세요.');
      return;
    }
    setIsConnected(true);
  };

  const handleDisconnect = () => {
    setIsConnected(false);
  };

  const handleStockSelect = (stock: typeof popularStocks[0]) => {
    setStockCode(stock.code);
    setStockName(stock.name);
  };

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <h1>한국투자증권 실시간 주식 차트 테스트</h1>
        <p>실시간 주식 데이터를 받아와서 차트로 시각화하는 테스트 페이지입니다.</p>
      </div>

      {!isConnected ? (
        <div className={styles.setupPanel}>
          <div className={styles.credentialsForm}>
            <h2>API 인증 정보</h2>
            <div className={styles.inputGroup}>
              <label htmlFor="appkey">App Key</label>
              <input
                id="appkey"
                type="password"
                value={credentials.appkey}
                onChange={(e) => setCredentials(prev => ({ ...prev, appkey: e.target.value }))}
                placeholder="한국투자증권 App Key를 입력하세요"
                className={styles.input}
              />
            </div>
            
            <div className={styles.inputGroup}>
              <label htmlFor="appsecret">App Secret</label>
              <input
                id="appsecret"
                type="password"
                value={credentials.appsecret}
                onChange={(e) => setCredentials(prev => ({ ...prev, appsecret: e.target.value }))}
                placeholder="한국투자증권 App Secret을 입력하세요"
                className={styles.input}
              />
            </div>
          </div>

          <div className={styles.stockSelection}>
            <h2>종목 선택</h2>
            <div className={styles.stockGrid}>
              {popularStocks.map((stock) => (
                <button
                  key={stock.code}
                  onClick={() => handleStockSelect(stock)}
                  className={`${styles.stockButton} ${
                    stockCode === stock.code ? styles.selected : ''
                  }`}
                >
                  <div className={styles.stockButtonName}>{stock.name}</div>
                  <div className={styles.stockButtonCode}>{stock.code}</div>
                </button>
              ))}
            </div>
            
            <div className={styles.customStock}>
              <h3>직접 입력</h3>
              <div className={styles.customInputs}>
                <input
                  type="text"
                  value={stockCode}
                  onChange={(e) => setStockCode(e.target.value)}
                  placeholder="종목코드 (예: 005930)"
                  className={styles.input}
                />
                <input
                  type="text"
                  value={stockName}
                  onChange={(e) => setStockName(e.target.value)}
                  placeholder="종목명 (예: 삼성전자)"
                  className={styles.input}
                />
              </div>
            </div>
          </div>

          <div className={styles.connectSection}>
            <div className={styles.selectedInfo}>
              <strong>선택된 종목:</strong> {stockName} ({stockCode})
            </div>
            <button
              onClick={handleConnect}
              className={styles.connectButton}
              disabled={!credentials.appkey || !credentials.appsecret || !stockCode}
            >
              실시간 차트 연결
            </button>
          </div>

          <div className={styles.warning}>
            <h3>⚠️ 주의사항</h3>
            <ul>
              <li>이 페이지는 테스트 목적으로만 사용하세요.</li>
              <li>실제 배포 시에는 API 키가 클라이언트에 노출되지 않도록 서버를 통해 처리해야 합니다.</li>
              <li>한국투자증권 API 사용 제한이 있을 수 있습니다.</li>
              <li>시장 시간 외에는 실시간 데이터가 제공되지 않을 수 있습니다.</li>
            </ul>
          </div>
        </div>
      ) : (
        <div className={styles.chartSection}>
          <div className={styles.controls}>
            <button
              onClick={handleDisconnect}
              className={styles.disconnectButton}
            >
              연결 해제
            </button>
          </div>
          
          <RealTimeChart
            appkey={credentials.appkey}
            appsecret={credentials.appsecret}
            stockCode={stockCode}
            stockName={stockName}
          />
        </div>
      )}
    </div>
  );
}