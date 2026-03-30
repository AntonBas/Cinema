import React from 'react';
import type { DashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import { DashboardStatsCard } from '../DashboardStatsCard/DashboardStatsCard';
import styles from './DashboardStatsGrid.module.css';

interface DashboardStatsGridProps {
    stats: DashboardStats;
    formatCurrency: (amount: number) => string;
}

export const DashboardStatsGrid: React.FC<DashboardStatsGridProps> = ({ stats, formatCurrency }) => {
    const statsCards = [
        { icon: '🎬', number: stats.totalMovies, label: 'Total Movies', subtext: `${stats.todaysStats.activeMovies} active` },
        { icon: '🏛️', number: stats.totalHalls, label: 'Cinema Halls' },
        { icon: '⏰', number: stats.totalSessions, label: 'Total Sessions', subtext: `${stats.todaySessions} today`, trend: stats.upcomingSessions },
        { icon: '👥', number: stats.totalUsers, label: 'Total Users', subtext: `${stats.todaysStats.newUsers} today` },
        { icon: '🎭', number: stats.activeScreenings, label: 'Active Screenings', subtext: `${stats.todaysStats.sessionsCompleted} completed` },
        { icon: '🎁', number: stats.activePromotions, label: 'Active Promotions' },
        { icon: '🎫', number: stats.totalTicketsSold, label: 'Tickets Sold', subtext: `${stats.todaysStats.ticketsSold} today` },
        { icon: '💰', number: formatCurrency(stats.totalRevenue), label: 'Total Revenue', subtext: `${formatCurrency(stats.todaysStats.revenue)} today` },
        { icon: '📊', number: `${stats.averageOccupancyRate}%`, label: 'Avg Occupancy Rate' }
    ];

    return (
        <div className={styles.stats}>
            {statsCards.map((card, index) => (
                <DashboardStatsCard key={index} {...card} />
            ))}
        </div>
    );
};