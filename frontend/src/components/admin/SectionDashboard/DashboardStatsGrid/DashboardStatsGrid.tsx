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
        { icon: '🎬', number: stats.todaysStats.activeMovies, label: 'Active Movies', subtext: `${stats.totalMovies} total` },
        { icon: '🏛️', number: stats.totalHalls, label: 'Cinema Halls' },
        { icon: '⏰', number: stats.todaySessions, label: 'Today Sessions', subtext: `${stats.totalSessions} total`, trend: stats.upcomingSessions },
        { icon: '👥', number: stats.todaysStats.activeUsers, label: 'Active Users Today', subtext: `${stats.totalUsers} total` },
        { icon: '🎭', number: stats.todaysStats.sessionsCompleted, label: 'Completed Today', subtext: `${stats.activeScreenings} active` },
        { icon: '🎁', number: stats.activePromotions, label: 'Active Promotions' },
        { icon: '🎫', number: stats.todaysStats.ticketsSold, label: 'Tickets Sold Today', subtext: `${stats.totalTicketsSold} total` },
        { icon: '💰', number: formatCurrency(stats.todaysStats.revenue), label: 'Revenue Today', subtext: `${formatCurrency(stats.totalRevenue)} total` },
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