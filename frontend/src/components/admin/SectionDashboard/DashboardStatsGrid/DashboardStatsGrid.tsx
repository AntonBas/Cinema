import React from 'react';
import type { DashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import { DashboardStatsCard } from '../DashboardStatsCard/DashboardStatsCard';
import styles from './DashboardStatsGrid.module.css';

interface DashboardStatsGridProps {
    stats: DashboardStats;
}

export const DashboardStatsGrid: React.FC<DashboardStatsGridProps> = ({ stats }) => {
    const statsCards = [
        { icon: '🎬', number: stats.totalMovies, label: 'Total Movies' },
        { icon: '🏛️', number: stats.totalHalls, label: 'Cinema Halls' },
        { icon: '⏰', number: stats.totalSessions, label: 'Total Sessions' },
        { icon: '👥', number: stats.totalUsers, label: 'Total Users' },
        { icon: '🎁', number: stats.activePromotions, label: 'Active Promotions' },
    ];

    return (
        <div className={styles.stats}>
            {statsCards.map((card, index) => (
                <DashboardStatsCard key={index} {...card} />
            ))}
        </div>
    );
};