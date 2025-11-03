import React from 'react';
import { PersonRole } from '@/types/person';
import styles from './PersonTabs.module.css';

interface PersonTabsProps {
    activeTab: PersonRole | 'ALL';
    onTabChange: (tab: PersonRole | 'ALL') => void;
    stats: {
        ALL: number;
        [PersonRole.ACTOR]: number;
        [PersonRole.DIRECTOR]: number;
        [PersonRole.SCREENWRITER]: number;
    };
}

export const PersonTabs: React.FC<PersonTabsProps> = ({
    activeTab,
    onTabChange,
    stats,
}) => {
    const tabs = [
        {
            id: 'ALL' as const,
            label: 'All People',
            icon: '👥',
            count: stats.ALL,
        },
        {
            id: PersonRole.ACTOR,
            label: 'Actors',
            icon: '🎭',
            count: stats[PersonRole.ACTOR],
        },
        {
            id: PersonRole.DIRECTOR,
            label: 'Directors',
            icon: '🎬',
            count: stats[PersonRole.DIRECTOR],
        },
        {
            id: PersonRole.SCREENWRITER,
            label: 'Screenwriters',
            icon: '✍️',
            count: stats[PersonRole.SCREENWRITER],
        },
    ];

    return (
        <div className={styles.tabs}>
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    className={`${styles.tab} ${activeTab === tab.id ? styles.tabActive : ''}`}
                    onClick={() => onTabChange(tab.id)}
                    type="button"
                >
                    <span className={styles.tabIcon}>{tab.icon}</span>
                    <span className={styles.tabLabel}>{tab.label}</span>
                    <span className={styles.tabCount}>{tab.count}</span>
                </button>
            ))}
        </div>
    );
};