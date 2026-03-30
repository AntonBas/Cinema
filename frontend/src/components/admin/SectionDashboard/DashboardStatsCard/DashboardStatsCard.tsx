import React from 'react';
import styles from './DashboardStatsCard.module.css';

interface DashboardStatsCardProps {
    icon: string;
    number: number | string;
    label: string;
    subtext?: string;
    trend?: number;
}

export const DashboardStatsCard: React.FC<DashboardStatsCardProps> = ({
    icon,
    number,
    label,
    subtext,
    trend
}) => {
    return (
        <div className={styles.statCard}>
            <div className={styles.statHeader}>
                <div className={styles.statIcon}>{icon}</div>
                {trend !== undefined && trend > 0 && (
                    <div className={styles.statTrend}>↑{trend}</div>
                )}
            </div>
            <div className={styles.statContent}>
                <p className={styles.statNumber}>{number}</p>
                <span className={styles.statLabel}>{label}</span>
                {subtext && <span className={styles.statSubtext}>{subtext}</span>}
            </div>
        </div>
    );
};