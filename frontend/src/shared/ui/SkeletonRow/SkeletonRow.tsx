import React from 'react';

interface SkeletonRowProps {
  layout: 'search' | 'ranking';
  index: number;
  className?: string;
  // 각 요소별 스타일 클래스
  evenRowClassName?: string;
  layoutClassName?: string;
  iconClassName?: string;
  textClassName?: string;
  stockInfoClassName?: string;
  rankClassName?: string;
  stockNameClassName?: string;
  priceClassName?: string;
  changeClassName?: string;
  volumeClassName?: string;
}

export default function SkeletonRow({
  layout,
  index,
  className = '',
  evenRowClassName = '',
  layoutClassName = '',
  iconClassName = '',
  textClassName = '',
  stockInfoClassName = '',
  rankClassName = '',
  stockNameClassName = '',
  priceClassName = '',
  changeClassName = '',
  volumeClassName = ''
}: SkeletonRowProps) {
  const rowClasses = `${className} ${index % 2 === 0 ? evenRowClassName : ''}`.trim();

  if (layout === 'search') {
    return (
      <div className={rowClasses} style={{ pointerEvents: 'none' }}>
        <div className={layoutClassName}>
          <div className={iconClassName}></div>
          <div className={textClassName}></div>
        </div>
      </div>
    );
  }

  if (layout === 'ranking') {
    return (
      <div className={rowClasses} style={{ pointerEvents: 'none' }}>
        {/* 순위 + 종목명 (첫 번째 컬럼) */}
        <div className={stockInfoClassName}>
          <div className={rankClassName}></div>
          <div className={stockNameClassName}></div>
        </div>

        {/* 현재가 (두 번째 컬럼, 우측 정렬) */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
          <div className={priceClassName}></div>
        </div>

        {/* 등락률 (세 번째 컬럼, 우측 정렬) */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
          <div className={changeClassName}></div>
        </div>

        {/* 거래량 (네 번째 컬럼, 우측 정렬) */}
        <div style={{ display: 'flex', justifyContent: 'flex-end', width: '100%' }}>
          <div className={volumeClassName}></div>
        </div>
      </div>
    );
  }

  return null;
}