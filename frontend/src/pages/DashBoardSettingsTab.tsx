import styles from './DashboardPage.module.css';

export default function DashBoardSettingsTab() {
    return (
        <>
        {/* 리밸런싱 실행 설정*/}
                    <div className={styles.rebalancingControls}>
                    <div className={styles.controlGroup}>
                        <h3>즉시 실행</h3>
                        <button className={styles.executeButton}>
                        <svg width="19" height="19" viewBox="0 0 19 19" fill="none">
                            <path d="M4.75589 2.38672L15.7495 9.33007L4.75589 16.2734V2.38672Z" fill="white" stroke="white" strokeWidth="1.38867" strokeLinecap="round" strokeLinejoin="round"/>
                        </svg>
                        지금 리벨런싱 실행
                        </button>
                    </div>

                    <div className={styles.controlGroup}>
                        <h3>자동 리벨런싱</h3>
                        <div className={styles.toggleContainer}>
                        <div className={styles.toggle}>
                            <div className={styles.toggleTrack}></div>
                            <div className={styles.toggleThumb}></div>
                        </div>
                        <span className={styles.toggleLabel}>활성화</span>
                        </div>
                    </div>

                    <div className={styles.controlGroup}>
                        <h3>리밸런싱 주기</h3>
                        <div className={styles.periodButtons}>
                        <button className={styles.periodButton}>주간</button>
                        <button className={`${styles.periodButton} ${styles.active}`}>월간</button>
                        <button className={styles.periodButton}>연간</button>
                        <input type="number" className={styles.periodInput} defaultValue="3" />
                        <span>개월마다</span>
                        <button className={styles.saveButton}>저장</button>
                        </div>
                    </div>
                    </div>
                    </>
    )
}