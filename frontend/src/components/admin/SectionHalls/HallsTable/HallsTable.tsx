import React from 'react';
import type { CinemaHallResponse } from '@/types';
import { Button, Badge } from '@/components/ui';
import LoadingSpinner from '@/components/ui/LoadingSpinner/LoadingSpinner';
import styles from './HallsTable.module.css';

interface HallsTableProps {
    halls: CinemaHallResponse[];
    onDelete: (hall: CinemaHallResponse) => void;
    onShowLayout: (hall: CinemaHallResponse) => void;
    onEdit?: (hall: CinemaHallResponse) => void;
    loading?: boolean;
}

export const HallsTable: React.FC<HallsTableProps> = ({
    halls,
    onDelete,
    onShowLayout,
    onEdit,
    loading = false
}) => {
    if (loading) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner />
                <p>Loading cinema halls...</p>
            </div>
        );
    }

    if (halls.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>🎭</div>
                <h3>No Cinema Halls</h3>
                <p>Create your first cinema hall to get started</p>
            </div>
        );
    }

    return (
        <div className={styles.table}>
            <div className={styles.tableHeader}>
                <div>Name</div>
                <div>Capacity</div>
                <div>Actions</div>
            </div>
            {halls.map(hall => (
                <div key={hall.id} className={styles.tableRow}>
                    <div className={styles.name}>
                        <span>{hall.name}</span>
                    </div>
                    <div className={styles.capacity}>
                        <Badge variant="primary">
                            {hall.capacity} seats
                        </Badge>
                    </div>
                    <div className={styles.actions}>
                        <Button
                            variant="primary"
                            size="small"
                            onClick={() => onShowLayout(hall)}
                            className={styles.layoutButton}
                        >
                            Layout
                        </Button>
                        {onEdit && (
                            <Button
                                variant="success"
                                size="small"
                                onClick={() => onEdit(hall)}
                                className={styles.editButton}
                            >
                                Edit
                            </Button>
                        )}
                        <Button
                            variant="error"
                            size="small"
                            onClick={() => onDelete(hall)}
                            className={styles.deleteButton}
                        >
                            Delete
                        </Button>
                    </div>
                </div>
            ))}
        </div>
    );
};