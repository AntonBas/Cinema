import React from 'react';
import type { PersonDto } from '@/types/person';
import { PersonRole } from '@/types/person';
import styles from './PersonCard.module.css';

export interface PersonCardProps {
    person: PersonDto;
    onEdit: (person: PersonDto) => void;
    onDelete: (person: PersonDto) => void;
}

export const PersonCard: React.FC<PersonCardProps> = ({
    person,
    onEdit,
    onDelete
}) => {
    const getRoleConfig = (role: PersonRole) => {
        switch (role) {
            case PersonRole.ACTOR:
                return {
                    icon: '🎭',
                    color: '#4CAF50',
                    label: 'Actor'
                };
            case PersonRole.DIRECTOR:
                return {
                    icon: '🎬',
                    color: '#2196F3',
                    label: 'Director'
                };
            case PersonRole.SCREENWRITER:
                return {
                    icon: '✍️',
                    color: '#FF9800',
                    label: 'Screenwriter'
                };
            default:
                return {
                    icon: '👤',
                    color: '#6b7280',
                    label: 'Person'
                };
        }
    };

    const roleConfig = getRoleConfig(person.role);

    return (
        <div className={styles.card}>
            <div className={styles.cardContent}>
                <div className={styles.header}>
                    <span
                        className={styles.roleIcon}
                        style={{ color: roleConfig.color }}
                        aria-label={roleConfig.label}
                    >
                        {roleConfig.icon}
                    </span>
                    <div className={styles.nameContainer}>
                        <h3 className={styles.name}>
                            {person.name}
                        </h3>
                    </div>
                </div>
                <div className={styles.details}>
                    <span
                        className={styles.roleBadge}
                        style={{
                            backgroundColor: `${roleConfig.color}20`,
                            color: roleConfig.color,
                            border: `1px solid ${roleConfig.color}40`
                        }}
                    >
                        {roleConfig.label}
                    </span>
                </div>
                <div className={styles.actions}>
                    <button
                        className={styles.editButton}
                        onClick={() => onEdit(person)}
                        type="button"
                        aria-label={`Edit ${person.name}`}
                    >
                        Edit
                    </button>
                    <button
                        className={styles.deleteButton}
                        onClick={() => onDelete(person)}
                        type="button"
                        aria-label={`Delete ${person.name}`}
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};