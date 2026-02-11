import React from 'react';
import { usePromotion } from '@/hooks/features/promotion/usePromotion';
import styles from './PromotionStats.module.css';

const PromotionStats: React.FC = () => {
    const { allPromotions } = usePromotion();

    const getPromotionStatus = (promotion: any) => {
        const now = new Date();
        const startDate = new Date(promotion.startDate);
        const endDate = new Date(promotion.endDate);

        if (now < startDate) return 'upcoming';
        if (now > endDate) return 'expired';
        return 'active';
    };

    const stats = {
        total: allPromotions.length,
        active: allPromotions.filter(p => getPromotionStatus(p) === 'active').length,
        upcoming: allPromotions.filter(p => getPromotionStatus(p) === 'upcoming').length,
        expired: allPromotions.filter(p => getPromotionStatus(p) === 'expired').length
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