import styles from './NewsWidget.module.css';
import { useLatestNews } from '../../features/news/hooks/useNews';
import type { NewsItem } from '../../features/news/api/types';

export default function NewsWidget() {
  const { data: newsData, isLoading, error } = useLatestNews();

  const handleNewsClick = (url: string, event: React.MouseEvent) => {
    event.preventDefault();
    window.open(url, '_blank', 'noopener,noreferrer');
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));

    if (diffInHours < 1) {
      return '방금 전';
    } else if (diffInHours < 24) {
      return `${diffInHours}시간 전`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays}일 전`;
    }
  };

  return (
    <div className={styles.newsSidebar}>
      <div className={styles.newsHeader}>
        <h3>추천 뉴스</h3>
      </div>

      <div className={styles.newsList}>
        {isLoading && (
          <div className={styles.loadingState}>
            <p>뉴스를 불러오는 중...</p>
          </div>
        )}

        {error && (
          <div className={styles.errorState}>
            <p>뉴스를 불러올 수 없습니다</p>
          </div>
        )}

        {newsData?.news && newsData.news.length > 0
          ? newsData.news.map((news: NewsItem) => (
              <div key={news.id} className={styles.newsItem} onClick={(e) => handleNewsClick(news.url, e)}>
                <div className={styles.newsContent}>
                  <h4 className={styles.newsTitle}>{news.title}</h4>
                  <p className={styles.newsSummary}>{news.summary}</p>
                  <span className={styles.newsTime}>{formatDate(news.publishedAt)}</span>
                </div>
                <div className={styles.newsImage}>
                  {news.imageUrl ? (
                    <img
                      src={news.imageUrl}
                      alt={news.title}
                      onError={(e) => {
                        e.currentTarget.style.display = 'none';
                      }}
                    />
                  ) : (
                    <div className={styles.noImage}>No Image</div>
                  )}
                </div>
              </div>
            ))
          : !isLoading &&
            !error && (
              <div className={styles.emptyState}>
                <p>표시할 뉴스가 없습니다</p>
              </div>
            )}
      </div>
    </div>
  );
}
