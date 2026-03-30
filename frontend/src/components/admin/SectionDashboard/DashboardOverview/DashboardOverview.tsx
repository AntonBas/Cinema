import React from 'react';
import type { DashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import styles from './DashboardOverview.module.css';

interface DashboardOverviewProps {
    stats: DashboardStats;
    formatCurrency: (amount: number) => string;
}

export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ stats, formatCurrency }) => {
    return (
        <div className={styles.overviewCard}>
            <div className={styles.overviewHeader}>
                <span className={styles.overviewTitle}>Today's Overview</span>
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
                    <span className={styles.overviewLabel}>Active Movies</span>
                    <span className={styles.overviewValue}>{stats.todaysStats.activeMovies}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Tickets Sold</span>
                    <span className={styles.overviewValue}>{stats.todaysStats.ticketsSold}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>Revenue</span>
                    <span className={styles.overviewValue}>{formatCurrency(stats.todaysStats.revenue)}</span>
                </div>
                <div className={styles.overviewStat}>
                    <span className={styles.overviewLabel}>New Users</span>
                    <span className={styles.overviewValue}>{stats.todaysStats.newUsers}</span>
                </div>
            </div>
        </div>
    );
};