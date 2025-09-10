import { useState, useEffect, useRef } from 'react';
import styles from './MarketTicker.module.css';

export default function MarketTicker() {
  const containerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const animationRef = useRef<number | null>(null);

  const marketData = [
    {
      name: 'KOSPI',
      value: '2,486.669',
      change: '+8.45',
      changePercent: '(0.31%)',
      isNegative: false,
    },
    {
      name: 'KOSDAQ',
      value: '742.308',
      change: '+3.39',
      changePercent: '(0.73%)',
      isNegative: false,
    },
    {
      name: 'KOSPI200',
      value: '338.582',
      change: '+3.94',
      changePercent: '(0.90%)',
      isNegative: false,
    },
    {
      name: 'USD/KRW',
      value: '1,347.836',
      change: '+5.12',
      changePercent: '(0.36%)',
      isNegative: false,
    },
  ];

  const [lastUpdateTime] = useState(() => new Date().toLocaleTimeString());

  useEffect(() => {
    const container = containerRef.current;
    const content = contentRef.current;
    if (!container || !content) return;

    let translateX = 0;
    const speed = 0.5;

    const animate = () => {
      if (!content) return;

      translateX -= speed;

      // 첫 번째 아이템이 화면을 완전히 벗어났는지 체크
      const firstItem = content.children[0] as HTMLElement;
      if (firstItem) {
        const firstItemRect = firstItem.getBoundingClientRect();
        const containerRect = container.getBoundingClientRect();

        // 첫 번째 아이템이 완전히 왼쪽으로 사라졌을 때
        if (firstItemRect.right < containerRect.left) {
          // 첫 번째 아이템을 맨 뒤로 이동 (DOM 조작)
          content.appendChild(firstItem);
          // 위치 조정: 아이템을 뒤로 보낸 만큼 전체를 오른쪽으로
          translateX += firstItemRect.width + 28; // width + gap
        }
      }

      // transform 적용
      content.style.transform = `translateX(${translateX}px)`;
      animationRef.current = requestAnimationFrame(animate);
    };

    // 초기 시작 전 약간의 지연
    setTimeout(() => {
      animationRef.current = requestAnimationFrame(animate);
    }, 100);

    return () => {
      if (animationRef.current) {
        cancelAnimationFrame(animationRef.current);
      }
    };
  }, []);

  const renderMarketItems = () => {
    return marketData.map((item, index) => (
      <div key={index} className={styles.tickerItem}>
        <div className={styles.itemName}>{item.name}</div>
        <div className={styles.itemValue}>{item.value}</div>
        <div className={styles.changeContainer}>
          <div className={`${styles.changeIcon} ${item.isNegative ? styles.negative : styles.positive}`}>
            {item.isNegative ? (
              <svg viewBox='0 0 11 6' fill='none' xmlns='http://www.w3.org/2000/svg'>
                <path
                  d='M9.75 5.375L6.03125 1.65625L3.84375 3.84375L1 1'
                  stroke='#e7000b'
                  strokeWidth='0.875'
                  strokeLinecap='round'
                  strokeLinejoin='round'
                />
              </svg>
            ) : (
              <svg viewBox='0 0 11 6' fill='none' xmlns='http://www.w3.org/2000/svg'>
                <path
                  d='M1 5L4.71875 1.28125L6.90625 3.46875L9.75 0.625'
                  stroke='#0066cc'
                  strokeWidth='0.875'
                  strokeLinecap='round'
                  strokeLinejoin='round'
                />
              </svg>
            )}
          </div>
          <div className={styles.changeText}>
            <span className={`${styles.changeValue} ${item.isNegative ? styles.negative : styles.positive}`}>
              {item.change}
            </span>
            <span className={`${styles.changePercent} ${item.isNegative ? styles.negative : styles.positive}`}>
              {item.changePercent}
            </span>
          </div>
        </div>
      </div>
    ));
  };

  return (
    <div className={styles.ticker}>
      <div className={styles.container}>
        <div className={styles.scrollContainer} ref={containerRef}>
          <div className={styles.scrollContent} ref={contentRef}>
            {renderMarketItems()}
            {renderMarketItems()}
            {renderMarketItems()}
            {renderMarketItems()}
          </div>
        </div>
        <div className={styles.updateInfo}>• 10초마다 자동 업데이트 (마지막 업데이트: {lastUpdateTime})</div>
      </div>
    </div>
  );
}
