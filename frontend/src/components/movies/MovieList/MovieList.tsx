import React from 'react';
import type { MovieCardResponse } from '@/types/movie';
import { MovieCard } from '../MovieCard/MovieCard';
import { LoadingSpinner, Button } from '@/components/ui';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieCardResponse[];
    loading?: boolean;
    error?: Error | null;
    emptyMessage?: string;
    onRetry?: () => void;
}

export const MovieList: React.FC<MovieListProps> = React.memo(({
    movies,
    loading = false,
    error = null,
    emptyMessage = "No movies found",
    onRetry
}) => {
    if (error) {
        return (
            <div className={styles.error} role="alert">
                <div className={styles.errorIcon}>⚠️</div>
                <h3>Error loading movies</h3>
                <p>{error.message}</p>
                {onRetry && (
                    <Button
                        variant="primary"
                        onClick={onRetry}
                        aria-label="Try again"
                    >
                        Try Again
                    </Button>
                )}
            </div>
        );
    }

    if (loading) {
        return (
            <div className={styles.loading} aria-live="polite" aria-busy="true">
                <LoadingSpinner text="Loading movies..." />
            </div>
        );
    }

    if (!movies || movies.length === 0) {
        return (
            <div className={styles.empty} role="status">
                <div className={styles.emptyIcon}>🎬</div>
                <h3>{emptyMessage}</h3>
                <p>Try checking back later for new releases.</p>
            </div>
        );
    }

    return (
        <div className={styles.grid} role="list" aria-label="Movies list">
            {movies.map(movie => (
                <div key={movie.id} role="listitem">
                    <MovieCard movie={movie} />
                </div>
            ))}
        </div>
    );
});

MovieList.displayName = 'MovieList';