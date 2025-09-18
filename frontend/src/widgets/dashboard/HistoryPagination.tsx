import React, { useState, useEffect } from 'react';
import styles from './HistoryPagination.module.css';

interface HistoryPaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
}

export default function HistoryPagination({ currentPage, totalPages, onPageChange }: HistoryPaginationProps) {
  const [hoveredPage, setHoveredPage] = useState<number | null>(null);

  const handlePageClick = (page: number) => {
    if (page >= 1 && page <= totalPages && page !== currentPage) {
      setHoveredPage(null);
      onPageChange(page);
    }
  };

  const handleMouseEnter = (page: number) => {
    if (page !== currentPage) {
      setHoveredPage(page);
    }
  };

  const handleMouseLeave = () => {
    setHoveredPage(null);
  };

  // 현재 페이지가 변경될 때마다 hover 상태 리셋
  useEffect(() => {
    setHoveredPage(null);
  }, [currentPage]);

  const renderPageNumbers = () => {
    const pages: React.ReactNode[] = [];

    if (totalPages <= 5) {
      // 5페이지 이하: 모든 페이지 표시 (1 2 3 4 5)
      for (let i = 1; i <= totalPages; i++) {
        pages.push(
          <button
            key={`page-${i}`}
            className={styles.pageNumber}
            data-pagination-selected={i === currentPage ? '' : undefined}
            data-pagination-hovered={hoveredPage === i ? '' : undefined}
            onClick={() => handlePageClick(i)}
            onMouseEnter={() => handleMouseEnter(i)}
            onMouseLeave={handleMouseLeave}
          >
            {i}
          </button>
        );
      }
    } else if (currentPage < 5) {
      // 현재 페이지가 5 미만: 1 2 3 4 5 ... maxNum
      for (let i = 1; i <= 5; i++) {
        pages.push(
          <button
            key={`page-${i}`}
            className={styles.pageNumber}
            data-pagination-selected={i === currentPage ? '' : undefined}
            data-pagination-hovered={hoveredPage === i ? '' : undefined}
            onClick={() => handlePageClick(i)}
            onMouseEnter={() => handleMouseEnter(i)}
            onMouseLeave={handleMouseLeave}
          >
            {i}
          </button>
        );
      }

      if (totalPages > 6) {
        pages.push(
          <span key={`ellipsis-after-5-${currentPage}`} className={styles.ellipsis}>
            ...
          </span>
        );
      }

      pages.push(
        <button
          key={`page-last-${totalPages}`}
          className={styles.pageNumber}
          data-pagination-hovered={hoveredPage === totalPages ? '' : undefined}
          onClick={() => handlePageClick(totalPages)}
          onMouseEnter={() => handleMouseEnter(totalPages)}
          onMouseLeave={handleMouseLeave}
        >
          {totalPages}
        </button>
      );
    } else if (currentPage >= totalPages - 2) {
      // 현재 페이지가 끝부분에 가까움: 1 ... (totalPages-4) (totalPages-3) (totalPages-2) (totalPages-1) totalPages
      pages.push(
        <button
          key={`page-first-1`}
          className={styles.pageNumber}
          data-pagination-hovered={hoveredPage === 1 ? '' : undefined}
          onClick={() => handlePageClick(1)}
          onMouseEnter={() => handleMouseEnter(1)}
          onMouseLeave={handleMouseLeave}
        >
          1
        </button>
      );

      if (totalPages > 6) {
        pages.push(
          <span key={`ellipsis-before-end-${currentPage}`} className={styles.ellipsis}>
            ...
          </span>
        );
      }

      for (let i = totalPages - 4; i <= totalPages; i++) {
        if (i > 1) {
          pages.push(
            <button
              key={`page-end-${i}`}
              className={styles.pageNumber}
              data-pagination-selected={i === currentPage ? '' : undefined}
              data-pagination-hovered={hoveredPage === i ? '' : undefined}
              onClick={() => handlePageClick(i)}
              onMouseEnter={() => handleMouseEnter(i)}
              onMouseLeave={handleMouseLeave}
            >
              {i}
            </button>
          );
        }
      }
    } else {
      // 중간 부분: 1 ... (currentPage-1) currentPage (currentPage+1) ... totalPages
      pages.push(
        <button
          key={`page-first-1`}
          className={styles.pageNumber}
          data-pagination-hovered={hoveredPage === 1 ? '' : undefined}
          onClick={() => handlePageClick(1)}
          onMouseEnter={() => handleMouseEnter(1)}
          onMouseLeave={handleMouseLeave}
        >
          1
        </button>
      );

      pages.push(
        <span key={`ellipsis-start-${currentPage}`} className={styles.ellipsis}>
          ...
        </span>
      );

      for (let i = currentPage - 1; i <= currentPage + 1; i++) {
        pages.push(
          <button
            key={`page-middle-${i}`}
            className={styles.pageNumber}
            data-pagination-selected={i === currentPage ? '' : undefined}
            data-pagination-hovered={hoveredPage === i ? '' : undefined}
            onClick={() => handlePageClick(i)}
            onMouseEnter={() => handleMouseEnter(i)}
            onMouseLeave={handleMouseLeave}
          >
            {i}
          </button>
        );
      }

      pages.push(
        <span key={`ellipsis-end-${currentPage}`} className={styles.ellipsis}>
          ...
        </span>
      );

      pages.push(
        <button
          key={`page-last-${totalPages}`}
          className={styles.pageNumber}
          data-pagination-hovered={hoveredPage === totalPages ? '' : undefined}
          onClick={() => handlePageClick(totalPages)}
          onMouseEnter={() => handleMouseEnter(totalPages)}
          onMouseLeave={handleMouseLeave}
        >
          {totalPages}
        </button>
      );
    }

    return pages;
  };

  // 테스트용으로 임시 주석 처리
  // if (totalPages <= 1) {
  //   return null;
  // }

  return <div className={styles.pagination}>{renderPageNumbers()}</div>;
}