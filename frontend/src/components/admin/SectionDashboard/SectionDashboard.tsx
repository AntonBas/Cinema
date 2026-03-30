import React from 'react';
import { useDashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import { DashboardHeader } from './DashboardHeader/DashboardHeader';
import { DashboardOverview } from './DashboardOverview/DashboardOverview';
import { DashboardStatsGrid } from './DashboardStatsGrid/DashboardStatsGrid';
import styles from './SectionDashboard.module.css';

export const SectionDashboard: React.FC = () => {
  const { stats, isLoading, lastUpdated, refresh } = useDashboardStats();

  const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'UAH',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0
    }).format(amount);
  };

  if (isLoading || !stats) {
    return (
      <div className={styles.loading}>
        <div className={styles.loadingSpinner} />
        <p>Loading dashboard...</p>
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <DashboardHeader lastUpdated={lastUpdated} onRefresh={refresh} />
      <DashboardOverview stats={stats} formatCurrency={formatCurrency} />
      <DashboardStatsGrid stats={stats} formatCurrency={formatCurrency} />
    </div>
  );
};