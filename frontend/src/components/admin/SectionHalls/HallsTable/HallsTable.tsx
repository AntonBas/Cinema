import React from 'react';
import type { CinemaHallDto } from '@/types';
import styles from './HallsTable.module.css';

interface HallsTableProps {
    halls: CinemaHallDto[];
    onDelete: (hall: CinemaHallDto) => void;
    onShowLayout: (hall: CinemaHallDto) => void;
    onEdit?: (hall: CinemaHallDto) => void;
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
                        <button
                            className={styles.layoutButton}
                            onClick={() => onShowLayout(hall)}
                            title="View Layout"
                        >
                            🎭 Layout
                        </button>
                        {onEdit && (
                            <button
                                className={styles.editButton}
                                onClick={() => onEdit(hall)}
                                title="Edit Hall"
                            >
                                ✏️ Edit
                            </button>
                        )}
                        <button
                            className={styles.deleteButton}
                            onClick={() => onDelete(hall)}
                            title="Delete Hall"
                        >
                            🗑️ Delete
                        </button>
                    </div>
                </div>
            ))}
        </div>
    );
};