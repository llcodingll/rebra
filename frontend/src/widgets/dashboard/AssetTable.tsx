import styles from './AssetTable.module.css';

interface Stock {
  name: string;
  code: string;
  buyPrice: string;
  currentPrice: string;
  quantity: string;
  value: string;
  return: string;
  returnAmount: string;
  currentWeight: string;
  targetWeight: string;
  weight: string;
  threshold: string;
  type: 'registered' | 'unregistered';
}

interface AssetTableProps {
  title: string;
  type: 'registered' | 'unregistered';
  data: Stock[];
}

export default function AssetTable({ title, type, data }: AssetTableProps) {
  const registeredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률', '현재 비중(%)', '목표 비중(%)', '가중치', '임계값 비중(%)', '제외'
  ];

  const unregisteredColumns = [
    '종목명', '매수가/현재가', '수량/평가금액', '수익률'
  ];

  const columns = type === 'registered' ? registeredColumns : unregisteredColumns;

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
              {data.map((stock, index) => (
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