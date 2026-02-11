import React from 'react';
import { type PersonResponse, type PersonRole } from '@/types/person';
import { PersonCard } from '../PersonCard';
import { Button } from '@/components/ui';
import styles from './PersonList.module.css';

interface PersonListProps {
    persons: PersonResponse[];
    activeTab: PersonRole | 'ALL';
    onEdit: (person: PersonResponse) => void;
    onDelete: (person: PersonResponse) => void;
    onAddPerson: () => void;
}

export const PersonList: React.FC<PersonListProps> = ({
    persons,
    activeTab,
    onEdit,
    onDelete,
    onAddPerson,
}) => {
    const filteredPersons = activeTab === 'ALL'
        ? persons
        : persons.filter(person => person.role === activeTab);

    const getEmptyStateConfig = () => {
        switch (activeTab) {
            case 'ALL':
                return {
                    icon: '👥',
                    title: 'No people found',
                    description: 'Get started by adding your first person',
                    buttonText: 'Add Person'
                };
            case 'ACTOR':
                return {
                    icon: '🎭',
                    title: 'No actors found',
                    description: 'Add actors to appear in movie casts',
                    buttonText: 'Add Actor'
                };
            case 'DIRECTOR':
                return {
                    icon: '🎬',
                    title: 'No directors found',
                    description: 'Add directors to direct movies',
                    buttonText: 'Add Director'
                };
            case 'SCREENWRITER':
                return {
                    icon: '✍️',
                    title: 'No screenwriters found',
                    description: 'Add screenwriters to write movie scripts',
                    buttonText: 'Add Screenwriter'
                };
            default:
                return {
                    icon: '👥',
                    title: 'No people found',
                    description: 'Get started by adding your first person',
                    buttonText: 'Add Person'
                };
        }
    };

    if (filteredPersons.length === 0) {
        const emptyConfig = getEmptyStateConfig();

        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>
                    {emptyConfig.icon}
                </div>
                <h3>{emptyConfig.title}</h3>
                <p>{emptyConfig.description}</p>
                <Button
                    variant="primary"
                    onClick={onAddPerson}
                    className={styles.addButton}
                >
                    <span className={styles.buttonIcon}>+</span>
                    {emptyConfig.buttonText}
                </Button>
            </div>
        );
    }

    return (
        <div className={styles.grid}>
            {filteredPersons.map(person => (
                <PersonCard
                    key={person.id}
                    person={person}
                    onEdit={onEdit}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
};