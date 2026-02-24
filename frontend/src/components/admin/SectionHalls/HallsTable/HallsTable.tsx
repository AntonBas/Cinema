import React, { useCallback, useMemo } from 'react';
import type { CinemaHallResponse } from '@/types/cinemaHall';
import { Button, Badge } from '@/components/ui';
import styles from './HallsTable.module.css';

interface HallsTableProps {
    halls: CinemaHallResponse[];
    onDelete: (hall: CinemaHallResponse) => void;
    onShowLayout: (hall: CinemaHallResponse) => void;
    onEdit?: (hall: CinemaHallResponse) => void;
}

export const HallsTable: React.FC<HallsTableProps> = React.memo(({
    halls,
    onDelete,
    onShowLayout,
    onEdit
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

    const renderEmptyState = useCallback(() => (
        <div className={styles.empty}>
            <div className={styles.emptyIcon}>🎭</div>
            <h3>No Cinema Halls</h3>
            <p>Create your first cinema hall to get started</p>
        </div>
    ), []);

    const renderTableHeader = useCallback(() => (
        <div className={styles.tableHeader}>
            <div>Name</div>
            <div>Capacity</div>
            <div>Actions</div>
        </div>
    ), []);

    const renderTableRow = useCallback((hall: CinemaHallResponse) => (
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
    ), [onEdit, handleDelete, handleShowLayout, handleEdit]);

    const tableContent = useMemo(() => {
        if (halls.length === 0) {
            return renderEmptyState();
        }

        return (
            <div className={styles.table}>
                {renderTableHeader()}
                {halls.map(renderTableRow)}
            </div>
        );
    }, [halls, renderEmptyState, renderTableHeader, renderTableRow]);

    return tableContent;
});

HallsTable.displayName = 'HallsTable';