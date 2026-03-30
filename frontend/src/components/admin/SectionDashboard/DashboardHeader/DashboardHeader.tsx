import React from 'react';
import styles from './DashboardHeader.module.css';

interface DashboardHeaderProps {
    lastUpdated: string;
    onRefresh?: () => void;
}

export const DashboardHeader: React.FC<DashboardHeaderProps> = ({ lastUpdated, onRefresh }) => {
    return (
        <div className={styles.header}>
            <div>
                <h1>Admin Dashboard</h1>
                <div className={styles.subtitle}>
                    <span>Last updated: {lastUpdated}</span>
                </div>
            </div>
            {onRefresh && (
                <button onClick={onRefresh} className={styles.refreshButton}>
                    Refresh
                </button>
            )}
        </div>
    );
};