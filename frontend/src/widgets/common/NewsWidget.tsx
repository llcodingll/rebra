import styles from './NewsWidget.module.css';
import { simpleNewsData } from '../search/stockListData';

export default function NewsWidget() {
  return (
    <div className={styles.newsSidebar}>
      <div className={styles.newsHeader}>
        <h3>주요 뉴스</h3>
      </div>
      
      <div className={styles.newsList}>
        {simpleNewsData.map((news, index) => (
          <div key={index} className={styles.newsItem}>
            <div className={styles.newsContent}>
              <h4 className={styles.newsTitle}>{news.title}</h4>
              <span className={styles.newsTime}>{news.time}</span>
            </div>
            <div className={styles.newsImage}>
              <span>Image</span>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}