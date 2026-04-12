import React from 'react';
import { useDashboardStats } from '@/hooks/features/dashboard/useDashboardStats';
import { DashboardStatsGrid } from './DashboardStatsGrid/DashboardStatsGrid';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './SectionDashboard.module.css';

export const SectionDashboard: React.FC = () => {
  const { stats, isLoading } = useDashboardStats();

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
      <DashboardStatsGrid stats={stats} />
    </div>
  );
};