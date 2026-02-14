import React from 'react';
import type { GenreResponse } from '@/types/genre';
import { Button, Badge } from '@/components/ui';
import styles from './GenreTable.module.css';

interface GenreTableProps {
    genres: GenreResponse[];
    onEdit: (genre: GenreResponse) => void;
    onDelete: (genre: GenreResponse) => void;
    loading?: boolean;
}

export const GenreTable: React.FC<GenreTableProps> = ({
    genres,
    onEdit,
    onDelete,
    loading = false
}) => {
    if (loading) {
        return (
            <div className={styles.loading}>
                <div className={styles.loadingSpinner}></div>
                <p>Loading genres...</p>
            </div>
        );
    }

    if (genres.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>📚</div>
                <h3>No genres found</h3>
                <p>Create your first genre to get started!</p>
            </div>
        );
    }

    return (
        <div className={styles.table}>
            <div className={styles.tableHeader}>
                <div className={styles.headerCell}>Name</div>
                <div className={styles.headerCell}>Movies Count</div>
                <div className={styles.headerCell}>Actions</div>
            </div>
            <div className={styles.tableBody}>
                {genres.map((genre) => (
                    <div key={genre.id} className={styles.tableRow}>
                        <div className={styles.nameCell}>
                            <span className={styles.name}>{genre.name}</span>
                        </div>
                        <div className={styles.countCell}>
                            <Badge variant="primary">
                                {genre.movieCount} {genre.movieCount === 1 ? 'movie' : 'movies'}
                            </Badge>
                        </div>
                        <div className={styles.actionsCell}>
                            <div className={styles.actions}>
                                <Button
                                    variant="success"
                                    size="small"
                                    onClick={() => onEdit(genre)}
                                    className={styles.editButton}
                                >
                                    Edit
                                </Button>
                                <Button
                                    variant="error"
                                    size="small"
                                    onClick={() => onDelete(genre)}
                                    className={styles.deleteButton}
                                >
                                    Delete
                                </Button>
                            </div>
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};