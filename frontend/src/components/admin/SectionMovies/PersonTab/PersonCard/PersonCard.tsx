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
    const getRoleIcon = (role: PersonRole) => {
        switch (role) {
            case PersonRole.ACTOR: return '🎭';
            case PersonRole.DIRECTOR: return '🎬';
            case PersonRole.SCREENWRITER: return '✍️';
            default: return '👤';
        }
    };

    const getRoleColor = (role: PersonRole) => {
        switch (role) {
            case PersonRole.ACTOR: return '#4CAF50';
            case PersonRole.DIRECTOR: return '#2196F3';
            case PersonRole.SCREENWRITER: return '#FF9800';
            default: return '#6b7280';
        }
    };

    const getRoleLabel = (role: PersonRole) => {
        switch (role) {
            case PersonRole.ACTOR: return 'Actor';
            case PersonRole.DIRECTOR: return 'Director';
            case PersonRole.SCREENWRITER: return 'Screenwriter';
            default: return 'Person';
        }
    };

    const roleColor = getRoleColor(person.role);
    const roleIcon = getRoleIcon(person.role);
    const roleLabel = getRoleLabel(person.role);

    return (
        <div className={styles.card}>
            <div className={styles.cardContent}>
                <div className={styles.header}>
                    <span
                        className={styles.roleIcon}
                        style={{ color: roleColor }}
                    >
                        {roleIcon}
                    </span>
                    <h3 className={styles.name}>{person.name}</h3>
                </div>
                <div className={styles.details}>
                    <span
                        className={styles.roleBadge}
                        style={{
                            background: `${roleColor}20`,
                            color: roleColor,
                            border: `1px solid ${roleColor}`
                        }}
                    >
                        {roleLabel}
                    </span>
                </div>
                <div className={styles.actions}>
                    <button
                        className={styles.editButton}
                        onClick={() => onEdit(person)}
                        type="button"
                    >
                        Edit
                    </button>
                    <button
                        className={styles.deleteButton}
                        onClick={() => onDelete(person)}
                        type="button"
                    >
                        Delete
                    </button>
                </div>
            </div>
        </div>
    );
};