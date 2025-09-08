import styles from './Header.module.css';
import { imgSvg, imgSvg1 } from '../../assets/imports/svg-jp00s';
import imgPhoto14720996457855658Abf4Ff4E from "figma:asset/f2e0d0183a438e31fe7131ed2173548b7f21aea2.png";

export default function Header() {
  return (
    <header className={styles.header}>
      <div className={styles.container}>
        <div className={styles.leftSection}>
          <div className={styles.logo}>
            <div className={styles.logoIcon}>
              <span>R</span>
            </div>
            <h1 className={styles.logoText}>Rebra</h1>
          </div>
        </div>
        
        <div className={styles.rightSection}>
          <div className={styles.iconButton}>
            <img src={imgSvg1} alt="알림" />
            <div className={styles.badge}>3</div>
          </div>
          <div className={styles.iconButton}>
            <img src={imgSvg} alt="설정" />
          </div>
          <div className={styles.userInfo}>
            <div 
              className={styles.avatar}
              style={{ backgroundImage: `url(${imgPhoto14720996457855658Abf4Ff4E})` }}
            ></div>
            <span className={styles.userName}>김투자</span>
          </div>
        </div>
      </div>
    </header>
  );
}