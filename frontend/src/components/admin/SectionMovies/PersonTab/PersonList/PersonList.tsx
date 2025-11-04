import React from 'react';
import { type PersonDto, PersonRole } from '@/types/person';
import { PersonCard } from '../PersonCard';
import styles from './PersonList.module.css';

interface PersonListProps {
    persons: PersonDto[];
    activeTab: PersonRole | 'ALL';
    onEdit: (person: PersonDto) => void;
    onDelete: (person: PersonDto) => void;
    onAddPerson: () => void;
}

export const PersonList: React.FC<PersonListProps> = ({
    persons,
    activeTab,
    onEdit,
    onDelete,
    onAddPerson,
}) => {
    const getEmptyStateConfig = () => {
        switch (activeTab) {
            case 'ALL':
                return {
                    icon: '👥',
                    title: 'No people found',
                    description: 'Get started by adding your first person',
                    buttonText: 'Add Person'
                };
            case PersonRole.ACTOR:
                return {
                    icon: '🎭',
                    title: 'No actors found',
                    description: 'Add actors to appear in movie casts',
                    buttonText: 'Add Actor'
                };
            case PersonRole.DIRECTOR:
                return {
                    icon: '🎬',
                    title: 'No directors found',
                    description: 'Add directors to direct movies',
                    buttonText: 'Add Director'
                };
            case PersonRole.SCREENWRITER:
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

    if (persons.length === 0) {
        const emptyConfig = getEmptyStateConfig();

        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>
                    {emptyConfig.icon}
                </div>
                <h3>{emptyConfig.title}</h3>
                <p>{emptyConfig.description}</p>
                <button
                    className={styles.primaryButton}
                    onClick={onAddPerson}
                >
                    {emptyConfig.buttonText}
                </button>
            </div>
        );
    }

    return (
        <div className={styles.grid}>
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
};