import React from 'react';
import type { CinemaHallResponse } from '@/types';
import { Button } from '@/components/ui';
import styles from './HallsTable.module.css';

interface HallsTableProps {
    halls: CinemaHallResponse[];
    onDelete: (hall: CinemaHallResponse) => void;
    onShowLayout: (hall: CinemaHallResponse) => void;
    onEdit?: (hall: CinemaHallResponse) => void;
}

export const HallsTable: React.FC<HallsTableProps> = ({
    halls,
    onDelete,
    onShowLayout,
    onEdit
}) => {
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
                    <div className={styles.name}>{hall.name}</div>
                    <div className={styles.capacity}>{hall.capacity} seats</div>
                    <div className={styles.actions}>
                        <Button
                            variant="primary"
                            size="small"
                            onClick={() => onShowLayout(hall)}
                            className={styles.layoutButton}
                        >
                            Seat Layout
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