import React, { useCallback } from 'react';
import type { PersonRole } from '@/types/person';
import styles from './PersonTabs.module.css';

interface PersonTabsProps {
    activeTab: PersonRole | 'ALL';
    onTabChange: (tab: PersonRole | 'ALL') => void;
}

interface TabConfig {
    id: PersonRole | 'ALL';
    label: string;
    icon: string;
}

const TAB_CONFIGS: TabConfig[] = [
    {
        id: 'ALL',
        label: 'All People',
        icon: '👥'
    },
    {
        id: 'ACTOR',
        label: 'Actors',
        icon: '🎭'
    },
    {
        id: 'DIRECTOR',
        label: 'Directors',
        icon: '🎬'
    },
    {
        id: 'SCREENWRITER',
        label: 'Screenwriters',
        icon: '✍️'
    }
] as const;

export const PersonTabs: React.FC<PersonTabsProps> = React.memo(({
    activeTab,
    onTabChange,
}) => {
    const handleTabClick = useCallback((tabId: PersonRole | 'ALL') => {
        onTabChange(tabId);
    }, [onTabChange]);

    const handleKeyDown = useCallback((e: React.KeyboardEvent, tabId: PersonRole | 'ALL') => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            onTabChange(tabId);
        }
    }, [onTabChange]);

    return (
        <div
            className={styles.tabs}
            role="tablist"
            aria-label="Person categories"
        >
            {TAB_CONFIGS.map((tab) => (
                <button
                    key={tab.id}
                    className={`${styles.tab} ${activeTab === tab.id ? styles.tabActive : ''}`}
                    onClick={() => handleTabClick(tab.id)}
                    onKeyDown={(e) => handleKeyDown(e, tab.id)}
                    type="button"
                    role="tab"
                    aria-selected={activeTab === tab.id}
                    aria-controls={`${tab.id}-panel`}
                    id={`${tab.id}-tab`}
                    tabIndex={activeTab === tab.id ? 0 : -1}
                >
                    <span className={styles.tabIcon} aria-hidden="true">
                        {tab.icon}
                    </span>
                    <span className={styles.tabLabel}>{tab.label}</span>
                </button>
            ))}
        </div>
    );
});

PersonTabs.displayName = 'PersonTabs';