import React from 'react';
import { useDashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import { DashboardOverview } from './DashboardOverview/DashboardOverview';
import { DashboardStatsGrid } from './DashboardStatsGrid/DashboardStatsGrid';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionDashboard.module.css';

export const SectionDashboard: React.FC = () => {
  const { stats, isLoading } = useDashboardStats();

  const formatCurrency = (amount: number): string => {
    if (amount === 0) return '0 UAH';
    const formatted = Math.round(amount).toLocaleString('uk-UA');
    return `${formatted} UAH`;
  };

  if (isLoading || !stats) {
    return (
      <div className={styles.loading}>
        <LoadingSpinner text="Loading dashboard..." />
      </div>
    );
  }

  return (
    <div className={styles.container}>
      <div className={styles.header}>
        <div>
          <h1 className={styles.title}>Admin Dashboard</h1>
          <p className={styles.subtitle}>Overview of cinema system performance and statistics</p>
        </div>
      </div>
      <DashboardOverview stats={stats} formatCurrency={formatCurrency} />
      <DashboardStatsGrid stats={stats} formatCurrency={formatCurrency} />
    </div>
  );
};