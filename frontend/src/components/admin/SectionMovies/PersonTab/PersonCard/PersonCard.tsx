import React, { useMemo } from 'react';
import type { PersonListResponse, PersonRole } from '@/types/person';
import { PersonRoleDisplay } from '@/types/person';
import { Button, Badge } from '@/components/ui';
import styles from './PersonCard.module.css';

interface PersonCardProps {
    person: PersonListResponse;
    onEdit: (person: PersonListResponse) => void;
    onDelete: (person: PersonListResponse) => void;
}

interface RoleConfig {
    icon: string;
    variant: 'success' | 'primary' | 'warning' | 'secondary';
    label: string;
}

const ROLE_CONFIGS: Record<PersonRole, RoleConfig> = {
    ACTOR: {
        icon: '🎭',
        variant: 'success',
        label: PersonRoleDisplay.ACTOR
    },
    DIRECTOR: {
        icon: '🎬',
        variant: 'primary',
        label: PersonRoleDisplay.DIRECTOR
    },
    SCREENWRITER: {
        icon: '✍️',
        variant: 'warning',
        label: PersonRoleDisplay.SCREENWRITER
    }
};

export const PersonCard: React.FC<PersonCardProps> = React.memo(({
    person,
    onEdit,
    onDelete
}) => {
    const roleConfig = useMemo(() =>
        ROLE_CONFIGS[person.role] || {
            icon: '👤',
            variant: 'secondary' as const,
            label: 'Person'
        },
        [person.role]
    );

    const movieCount = person.movieCount || 0;
    const movieText = `${movieCount} ${movieCount === 1 ? 'movie' : 'movies'}`;

    return (
        <div className={styles.card}>
            <div className={styles.cardContent}>
                <div className={styles.header}>
                    <span className={styles.roleIcon} aria-label={roleConfig.label}>
                        {roleConfig.icon}
                    </span>
                    <div className={styles.nameContainer}>
                        <h3 className={styles.name}>{person.name}</h3>
                    </div>
                </div>

                <div className={styles.details}>
                    <Badge variant={roleConfig.variant} size="small">
                        {roleConfig.label}
                    </Badge>
                    <Badge variant="secondary" size="small" className={styles.movieCountBadge}>
                        🎬 {movieText}
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
});

PersonCard.displayName = 'PersonCard';