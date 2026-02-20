import React, { useCallback } from 'react';
import type { MovieCardResponse } from '@/types/movie';
import { MovieCard } from './MovieCard/MovieCard';
import { Button, LoadingSpinner } from '@/components/ui';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieCardResponse[];
    onEdit: (movie: MovieCardResponse) => void;
    onDelete: (movie: MovieCardResponse) => void;
    onCreateNew?: () => void;
    loading?: boolean;
    emptyMessage?: string;
}

export const MovieList: React.FC<MovieListProps> = React.memo(({
    movies,
    onEdit,
    onDelete,
    onCreateNew,
    loading = false,
    emptyMessage = "No movies found"
}) => {
    const handleEdit = useCallback((movie: MovieCardResponse) => {
        onEdit(movie);
    }, [onEdit]);

    const handleDelete = useCallback((movie: MovieCardResponse) => {
        onDelete(movie);
    }, [onDelete]);

    const handleCreateNew = useCallback(() => {
        onCreateNew?.();
    }, [onCreateNew]);

    if (loading) {
        return (
            <div className={styles.loadingContainer}>
                <LoadingSpinner text="Loading movies..." />
            </div>
        );
    }

    if (movies.length === 0) {
        return (
            <div className={styles.empty} role="status" aria-label="No movies found">
                <div className={styles.emptyIcon} aria-hidden="true">🎬</div>
                <h3>{emptyMessage}</h3>
                <p>Get started by creating your first movie</p>
                {onCreateNew && (
                    <Button
                        variant="primary"
                        onClick={handleCreateNew}
                        className={styles.createButton}
                        aria-label="Create first movie"
                    >
                        Create First Movie
                    </Button>
                )}
            </div>
        );
    }

    return (
        <div className={styles.grid} role="grid" aria-label="Movies list">
            {movies.map((movie) => (
                <div
                    key={movie.id}
                    role="gridcell"
                    className={styles.gridCell}
                >
                    <MovieCard
                        movie={movie}
                        onEdit={handleEdit}
                        onDelete={handleDelete}
                    />
                </div>
            ))}
        </div>
    );
});

MovieList.displayName = 'MovieList';