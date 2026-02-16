import React, { useCallback, useMemo } from 'react';
import type { CinemaHallResponse } from '@/types/cinemaHall';
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

export const HallsTable: React.FC<HallsTableProps> = React.memo(({
    halls,
    onDelete,
    onShowLayout,
    onEdit,
    loading = false
}) => {
    const handleDelete = useCallback((hall: CinemaHallResponse) => {
        onDelete(hall);
    }, [onDelete]);

    const handleShowLayout = useCallback((hall: CinemaHallResponse) => {
        onShowLayout(hall);
    }, [onShowLayout]);

    const handleEdit = useCallback((hall: CinemaHallResponse) => {
        onEdit?.(hall);
    }, [onEdit]);

    const tableContent = useMemo(() => {
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
                                onClick={() => handleShowLayout(hall)}
                                className={styles.layoutButton}
                            >
                                Layout
                            </Button>
                            {onEdit && (
                                <Button
                                    variant="success"
                                    size="small"
                                    onClick={() => handleEdit(hall)}
                                    className={styles.editButton}
                                >
                                    Edit
                                </Button>
                            )}
                            <Button
                                variant="error"
                                size="small"
                                onClick={() => handleDelete(hall)}
                                className={styles.deleteButton}
                            >
                                Delete
                            </Button>
                        </div>
                    </div>
                ))}
            </div>
        );
    }, [halls, loading, onEdit, handleDelete, handleShowLayout, handleEdit]);

    return tableContent;
});

HallsTable.displayName = 'HallsTable';