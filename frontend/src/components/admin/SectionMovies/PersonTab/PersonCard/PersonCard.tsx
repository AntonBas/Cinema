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
} as const;

export const PersonCard: React.FC<PersonCardProps> = React.memo(({
    person,
    onEdit,
    onDelete
}) => {
    const roleConfig = useMemo(() =>
        ROLE_CONFIGS[person.role] || {
            icon: '👤',
            variant: 'secondary',
            label: 'Person'
        },
        [person.role]
    );

    const movieCount = person.movieCount || 0;
    const movieText = useMemo(() =>
        `🎬 ${movieCount} ${movieCount === 1 ? 'movie' : 'movies'}`,
        [movieCount]
    );

    const handleEdit = useMemo(() =>
        () => onEdit(person),
        [onEdit, person]
    );

    const handleDelete = useMemo(() =>
        () => onDelete(person),
        [onDelete, person]
    );

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
                        {movieText}
                    </Badge>
                </div>

                <div className={styles.actions}>
                    <Button
                        variant="success"
                        size="small"
                        onClick={handleEdit}
                        className={styles.editButton}
                    >
                        Edit
                    </Button>
                    <Button
                        variant="error"
                        size="small"
                        onClick={handleDelete}
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