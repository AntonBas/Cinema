import React from 'react';
import type { PersonResponse, PersonRole } from '@/types/person';
import { PersonRoleDisplay } from '@/types/person';
import { Button, Badge } from '@/components/ui';
import styles from './PersonCard.module.css';

export interface PersonCardProps {
    person: PersonResponse;
    onEdit: (person: PersonResponse) => void;
    onDelete: (person: PersonResponse) => void;
}

export const PersonCard: React.FC<PersonCardProps> = ({
    person,
    onEdit,
    onDelete
}) => {
    const getRoleConfig = (role: PersonRole) => {
        switch (role) {
            case 'ACTOR':
                return {
                    icon: '🎭',
                    variant: 'success' as const,
                    label: PersonRoleDisplay.ACTOR
                };
            case 'DIRECTOR':
                return {
                    icon: '🎬',
                    variant: 'primary' as const,
                    label: PersonRoleDisplay.DIRECTOR
                };
            case 'SCREENWRITER':
                return {
                    icon: '✍️',
                    variant: 'warning' as const,
                    label: PersonRoleDisplay.SCREENWRITER
                };
            default:
                return {
                    icon: '👤',
                    variant: 'secondary' as const,
                    label: 'Person'
                };
        }
    };

    const roleConfig = getRoleConfig(person.role);
    const movieCount = person.movieCount || 0;

    return (
        <div className={styles.card}>
            <div className={styles.cardContent}>
                <div className={styles.header}>
                    <span
                        className={styles.roleIcon}
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
                    <Badge
                        variant={roleConfig.variant}
                        size="small"
                    >
                        {roleConfig.label}
                    </Badge>

                    <Badge
                        variant="info"
                        size="small"
                        className={styles.movieCountBadge}
                    >
                        🎬 {movieCount} {movieCount === 1 ? 'movie' : 'movies'}
                    </Badge>
                </div>

                <div className={styles.actions}>
                    <Button
                        variant="success"
                        size="small"
                        onClick={() => onEdit(person)}
                        className={styles.editButton}
                    >
                        Edit
                    </Button>
                    <Button
                        variant="error"
                        size="small"
                        onClick={() => onDelete(person)}
                        className={styles.deleteButton}
                    >
                        Delete
                    </Button>
                </div>
            </div>
        </div>
    );
};