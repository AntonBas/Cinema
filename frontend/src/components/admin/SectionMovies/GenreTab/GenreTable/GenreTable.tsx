import React, { useCallback } from 'react';
import type { GenreListResponse } from '@/types/genre';
import { Button, Badge } from '@/components/ui';
import styles from './GenreTable.module.css';

interface GenreTableProps {
    genres: GenreListResponse[];
    onEdit: (genre: GenreListResponse) => void;
    onDelete: (genre: GenreListResponse) => void;
}

export const GenreTable: React.FC<GenreTableProps> = React.memo(({
    genres,
    onEdit,
    onDelete
}) => {
    const getMovieCountText = useCallback((count: number): string =>
        `${count} ${count === 1 ? 'movie' : 'movies'}`,
        []
    );

    if (genres.length === 0) {
        return (
            <div className={styles.empty} role="status" aria-label="No genres found">
                <div className={styles.emptyIcon} aria-hidden="true">📚</div>
                <h3>No genres found</h3>
                <p>Create your first genre to get started!</p>
            </div>
        );
    }

    return (
        <div className={styles.table} role="table" aria-label="Genres list">
            <div className={styles.tableHeader} role="rowgroup">
                <div className={styles.headerCell} role="columnheader">Name</div>
                <div className={styles.headerCell} role="columnheader">Movies</div>
                <div className={styles.headerCell} role="columnheader">Actions</div>
            </div>
            <div className={styles.tableBody} role="rowgroup">
                {genres.map((genre) => (
                    <div
                        key={genre.id}
                        className={styles.tableRow}
                        role="row"
                    >
                        <div className={styles.nameCell} role="cell">
                            <span className={styles.name}>{genre.name}</span>
                        </div>
                        <div className={styles.countCell} role="cell">
                            <Badge
                                variant="primary"
                                aria-label={getMovieCountText(genre.movieCount)}
                            >
                                {getMovieCountText(genre.movieCount)}
                            </Badge>
                        </div>
                        <div className={styles.actionsCell} role="cell">
                            <div className={styles.actions}>
                                <Button
                                    variant="success"
                                    size="small"
                                    onClick={() => onEdit(genre)}
                                    className={styles.editButton}
                                    aria-label={`Edit ${genre.name}`}
                                >
                                    Edit
                                </Button>
                                <Button
                                    variant="error"
                                    size="small"
                                    onClick={() => onDelete(genre)}
                                    className={styles.deleteButton}
                                    aria-label={`Delete ${genre.name}`}
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
});

GenreTable.displayName = 'GenreTable';