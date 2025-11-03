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
    if (persons.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>
                    {activeTab === 'ALL' ? '👥' :
                        activeTab === PersonRole.ACTOR ? '🎭' :
                            activeTab === PersonRole.DIRECTOR ? '🎬' : '✍️'}
                </div>
                <h3>
                    {activeTab === 'ALL' ? 'No people found' :
                        activeTab === PersonRole.ACTOR ? 'No actors found' :
                            activeTab === PersonRole.DIRECTOR ? 'No directors found' : 'No screenwriters found'}
                </h3>
                <p>
                    {activeTab === 'ALL' ? 'Get started by adding your first person' :
                        activeTab === PersonRole.ACTOR ? 'Add actors to appear in movie casts' :
                            activeTab === PersonRole.DIRECTOR ? 'Add directors to direct movies' : 'Add screenwriters to write movie scripts'}
                </p>
                <button
                    className={styles.primaryButton}
                    onClick={onAddPerson}
                >
                    Add {activeTab === 'ALL' ? 'Person' :
                        activeTab === PersonRole.ACTOR ? 'Actor' :
                            activeTab === PersonRole.DIRECTOR ? 'Director' : 'Screenwriter'}
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