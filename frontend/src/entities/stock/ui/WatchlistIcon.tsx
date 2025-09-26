import React from 'react';

interface WatchlistIconProps {
  isFavorite: boolean;
  size?: number;
  onClick?: (e: React.MouseEvent) => void;
  className?: string;
}

export default function WatchlistIcon({
  isFavorite,
  size = 16,
  onClick,
  className = '',
}: WatchlistIconProps) {
  const handleClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    onClick?.(e);
  };

  return (
    <div
      className={className}
      onClick={handleClick}
      style={{
        cursor: onClick ? 'pointer' : 'default',
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        // padding: '6px',
      }}
      title={isFavorite ? '관심종목에서 제거' : '관심종목에 추가'}
    >
      {isFavorite ? (
        // 관심종목인 경우 - 채워진 하트
        <svg
          width={size}
          height={size}
          viewBox='0 0 24 24'
          fill='#ef1515'
          ref={(el) => {
            // 인라인 스타일 제거하여 CSS fill 속성이 적용되도록 함
            if (el) {
              el.style.fill = '';
            }
          }}
        >
          <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
        </svg>
      ) : (
        // 비관심종목인 경우 - 꽉찬 하트 (회색, 호버 시 진한 회색)
        <svg
          width={size}
          height={size}
          viewBox='0 0 24 24'
          fill='#999'
          // style={{}}
          onMouseEnter={(e) => {
            if (onClick) (e.currentTarget as SVGElement).style.fill = '#666';
          }}
          onMouseLeave={(e) => {
            if (onClick) (e.currentTarget as SVGElement).style.fill = '#999';
          }}
        >
          <path d='M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z' />
        </svg>
      )}
    </div>
  );
}
