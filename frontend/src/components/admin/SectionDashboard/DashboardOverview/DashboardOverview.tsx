import React from 'react';
import type { DashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import styles from './DashboardOverview.module.css';

interface DashboardOverviewProps {
    stats: DashboardStats;
}

export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ stats }) => {
    return (
        <div className={styles.overviewCard}>
            <div className={styles.overviewHeader}>
                <span className={styles.overviewTitle}>Overview</span>
                <span className={styles.overviewDate}>
                    {new Date().toLocaleDateString('en-US', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    })}
                </span>
            </div>
            <div className={styles.overviewStats}>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Total Movies</span>
                    <span className={styles.overviewValue}>{stats.totalMovies}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Total Halls</span>
                    <span className={styles.overviewValue}>{stats.totalHalls}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Total Sessions</span>
                    <span className={styles.overviewValue}>{stats.totalSessions}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Total Users</span>
                    <span className={styles.overviewValue}>{stats.totalUsers}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Active Promotions</span>
                    <span className={styles.overviewValue}>{stats.activePromotions}</span>
                </div>
            </div>
        </div>
    );
};