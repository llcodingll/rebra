import styles from './AssetTable.module.css';

interface AssetTableProps {
  title: string;
  type: 'registered' | 'unregistered';
}

export default function AssetTable({ title, type }: AssetTableProps) {
  const registeredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률', '현재 비중(%)', '목표 비중(%)', '가중치', '임계값 비중(%)', '제외'
  ];

  const unregisteredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률'
  ];

  const columns = type === 'registered' ? registeredColumns : unregisteredColumns;

  const stockData = [
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
      threshold: '10'
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
      threshold: '5'
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
      threshold: '10'
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
      threshold: '5'
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
      threshold: '3'
    }
  ];

  return (
    <div className={styles.tableContainer}>
      <div className={styles.tableCard}>
        <div className={styles.tableHeader}>
          <h3>{title}</h3>
        </div>
        
        <div className={styles.tableWrapper}>
          <table className={styles.table}>
            <thead>
              <tr className={styles.headerRow}>
                {columns.map((column, index) => (
                  <th key={index} className={styles.headerCell}>
                    {column}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {stockData.map((stock, index) => (
                <tr key={index} className={styles.dataRow}>
                  <td className={styles.dataCell}>
                    <div className={styles.stockName}>
                      <span className={styles.name}>{stock.name}</span>
                      <div className={styles.codeTag}>{stock.code}</div>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.priceInfo}>
                      <span className={styles.buyPrice}>{stock.buyPrice}</span>
                      <span className={styles.currentPrice}>{stock.currentPrice}</span>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.quantityInfo}>
                      <span className={styles.quantity}>{stock.quantity}</span>
                      <span className={styles.value}>{stock.value}</span>
                    </div>
                  </td>
                  <td className={styles.dataCell}>
                    <div className={styles.returnInfo}>
                      <div className={styles.returnWithIcon}>
                        <svg width="12.591" height="12.591" viewBox="0 0 13 7" fill="none">
                          <path d="M11.4922 1L7.03302 5.45919L4.40997 2.83614L1 6.24611" stroke="#155DFC" strokeWidth="1.04922" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        <span className={styles.returnPercent}>{stock.return}</span>
                      </div>
                      <span className={styles.returnAmount}>{stock.returnAmount}</span>
                    </div>
                  </td>
                  {type === 'registered' && (
                    <>
                      <td className={styles.dataCell}>
                        <span>{stock.currentWeight}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <input 
                          type="text" 
                          defaultValue={stock.targetWeight}
                          className={styles.inputField}
                        />
                      </td>
                      <td className={styles.dataCell}>
                        <span>{stock.weight}</span>
                      </td>
                      <td className={styles.dataCell}>
                        <input 
                          type="text" 
                          defaultValue={stock.threshold}
                          className={styles.inputField}
                        />
                      </td>
                      <td className={styles.dataCell}>
                        <button className={styles.removeButton}>
                          <svg width="20.385" height="20.385" viewBox="0 0 17 14" fill="none">
                            <path d="M2.05051 3.5H14.3535" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                            <path d="M6.15152 3.5V2.33333C6.15152 2.08696 6.26625 1.85054 6.47279 1.67426C6.67933 1.49799 6.95634 1.40007 7.24502 1.40007H9.15883C9.44751 1.40007 9.72453 1.49799 9.93107 1.67426C10.1376 1.85054 10.2523 2.08696 10.2523 2.33333V3.5M12.9864 3.5V11.6667C12.9864 11.913 12.8717 12.1494 12.6651 12.3257C12.4585 12.502 12.1815 12.5999 11.8928 12.5999H4.51102C4.22234 12.5999 3.94532 12.502 3.73879 12.3257C3.53225 12.1494 3.41751 11.913 3.41751 11.6667V3.5" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                          </svg>
                        </button>
                      </td>
                    </>
                  )}
                  {type === 'unregistered' && (
                    <td className={styles.dataCell}>
                      <button className={styles.addButton}>
                        <svg width="20.385" height="20.385" viewBox="0 0 17 14" fill="none">
                          <path d="M2.05051 3.5H14.3535" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                          <path d="M6.15152 3.5V2.33333C6.15152 2.08696 6.26625 1.85054 6.47279 1.67426C6.67933 1.49799 6.95634 1.40007 7.24502 1.40007H9.15883C9.44751 1.40007 9.72453 1.49799 9.93107 1.67426C10.1376 1.85054 10.2523 2.08696 10.2523 2.33333V3.5M12.9864 3.5V11.6667C12.9864 11.913 12.8717 12.1494 12.6651 12.3257C12.4585 12.502 12.1815 12.5999 11.8928 12.5999H4.51102C4.22234 12.5999 3.94532 12.502 3.73879 12.3257C3.53225 12.1494 3.41751 11.913 3.41751 11.6667V3.5" stroke="#EF4444" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                      </button>
                    </td>
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}