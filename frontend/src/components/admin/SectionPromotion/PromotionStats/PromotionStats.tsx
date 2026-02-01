import React from 'react';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import styles from './PromotionStats.module.css';

const PromotionStats: React.FC = () => {
    const { promotions, getPromotionStatus } = usePromotion();

    const stats = {
        total: promotions.length,
        active: promotions.filter(p => getPromotionStatus(p) === 'active').length,
        upcoming: promotions.filter(p => getPromotionStatus(p) === 'upcoming').length,
        expired: promotions.filter(p => getPromotionStatus(p) === 'expired').length
    };

    return (
        <div className={styles.container}>
            <div className={styles.statCard}>
                <p className={styles.statLabel}>Total</p>
                <p className={styles.statValue}>{stats.total}</p>
            </div>
            <div className={styles.statCard}>
                <p className={styles.statLabel}>Active</p>
                <p className={`${styles.statValue} ${styles.active}`}>{stats.active}</p>
            </div>
            <div className={styles.statCard}>
                <p className={styles.statLabel}>Upcoming</p>
                <p className={`${styles.statValue} ${styles.upcoming}`}>{stats.upcoming}</p>
            </div>
            <div className={styles.statCard}>
                <p className={styles.statLabel}>Expired</p>
                <p className={`${styles.statValue} ${styles.expired}`}>{stats.expired}</p>
            </div>
        </div>
    );
};

export default PromotionStats;