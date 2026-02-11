import React from 'react';
import type { PersonRole } from '@/types/person';
import { Badge } from '@/components/ui';
import styles from './PersonTabs.module.css';

interface PersonTabsProps {
    activeTab: PersonRole | 'ALL';
    onTabChange: (tab: PersonRole | 'ALL') => void;
    stats: {
        ALL: number;
        ACTOR: number;
        DIRECTOR: number;
        SCREENWRITER: number;
    };
}

interface TabConfig {
    id: PersonRole | 'ALL';
    label: string;
    icon: string;
    count: number;
    variant: 'primary' | 'secondary' | 'success' | 'warning' | 'error';
}

export const PersonTabs: React.FC<PersonTabsProps> = ({
    activeTab,
    onTabChange,
    stats,
}) => {
    const tabs: TabConfig[] = [
        {
            id: 'ALL',
            label: 'All People',
            icon: '👥',
            count: stats.ALL,
            variant: 'primary'
        },
        {
            id: 'ACTOR',
            label: 'Actors',
            icon: '🎭',
            count: stats.ACTOR,
            variant: 'success'
        },
        {
            id: 'DIRECTOR',
            label: 'Directors',
            icon: '🎬',
            count: stats.DIRECTOR,
            variant: 'primary'
        },
        {
            id: 'SCREENWRITER',
            label: 'Screenwriters',
            icon: '✍️',
            count: stats.SCREENWRITER,
            variant: 'warning'
        },
    ];

    return (
        <div className={styles.tabs} role="tablist" aria-label="Person categories">
            {tabs.map((tab) => (
                <button
                    key={tab.id}
                    className={`${styles.tab} ${activeTab === tab.id ? styles.tabActive : ''}`}
                    onClick={() => onTabChange(tab.id)}
                    type="button"
                    role="tab"
                    aria-selected={activeTab === tab.id}
                    aria-controls={`${tab.id}-panel`}
                    id={`${tab.id}-tab`}
                >
                    <span className={styles.tabIcon} aria-hidden="true">
                        {tab.icon}
                    </span>
                    <span className={styles.tabLabel}>{tab.label}</span>
                    <Badge
                        variant={activeTab === tab.id ? "primary" : "secondary"}
                        size="small"
                        className={styles.tabBadge}
                    >
                        {tab.count}
                    </Badge>
                </button>
            ))}
        </div>
    );
};