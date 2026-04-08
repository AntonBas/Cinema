import React, { useCallback, useMemo } from 'react';
import type { PersonListResponse, PersonRole } from '@/types/person';
import { PersonCard } from '../PersonCard/PersonCard';
import { Button } from '@/components/ui';
import styles from './PersonList.module.css';

interface PersonListProps {
    persons: PersonListResponse[];
    activeTab: PersonRole | 'ALL';
    onEdit: (person: PersonListResponse) => void;
    onDelete: (person: PersonListResponse) => void;
    onAddPerson: () => void;
}

interface EmptyStateConfig {
    icon: string;
    title: string;
    description: string;
    buttonText: string;
}

const EMPTY_STATE_CONFIGS: Record<PersonRole | 'ALL', EmptyStateConfig> = {
    ALL: {
        icon: '👥',
        title: 'No people found',
        description: 'Get started by adding your first person',
        buttonText: 'Add Person'
    },
    ACTOR: {
        icon: '🎭',
        title: 'No actors found',
        description: 'Add actors to appear in movie casts',
        buttonText: 'Add Actor'
    },
    DIRECTOR: {
        icon: '🎬',
        title: 'No directors found',
        description: 'Add directors to direct movies',
        buttonText: 'Add Director'
    },
    SCREENWRITER: {
        icon: '✍️',
        title: 'No screenwriters found',
        description: 'Add screenwriters to write movie scripts',
        buttonText: 'Add Screenwriter'
    }
} as const;

export const PersonList: React.FC<PersonListProps> = React.memo(({
    persons,
    activeTab,
    onEdit,
    onDelete,
    onAddPerson,
}) => {
    const emptyConfig = useMemo(() =>
        EMPTY_STATE_CONFIGS[activeTab] || EMPTY_STATE_CONFIGS.ALL,
        [activeTab]
    );

    const handleAddClick = useCallback(() => {
        onAddPerson();
    }, [onAddPerson]);

    if (persons.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon} aria-hidden="true">
                    {emptyConfig.icon}
                </div>
                <h3>{emptyConfig.title}</h3>
                <p>{emptyConfig.description}</p>
                <Button
                    variant="primary"
                    onClick={handleAddClick}
                    className={styles.addButton}
                    aria-label={emptyConfig.buttonText}
                >
                    <span className={styles.buttonIcon} aria-hidden="true">+</span>
                    {emptyConfig.buttonText}
                </Button>
            </div>
        );
    }

    return (
        <div className={styles.grid} role="grid" aria-label={`${activeTab} persons list`}>
            {persons.map(person => (
                <PersonCard
                    key={person.id}
                    person={person}
                    onEdit={onEdit}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
});

PersonList.displayName = 'PersonList';