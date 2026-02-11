import React from 'react';
import type { MovieCardResponse } from '@/types/movie';
import { MovieCard } from './MovieCard/MovieCard';
import { Button, LoadingSpinner } from '@/components/ui';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieCardResponse[];
    onEdit: (movie: MovieCardResponse) => void;
    onDelete: (movie: MovieCardResponse) => void;
    loading?: boolean;
    onCreateNew?: () => void;
}

export const MovieList: React.FC<MovieListProps> = ({
    movies,
    onEdit,
    onDelete,
    loading = false,
    onCreateNew
}) => {
    if (loading) {
        return (
            <div className={styles.loading}>
                <LoadingSpinner text="Loading movies..." />
            </div>
        );
    }

    if (movies.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>🎬</div>
                <h3>No movies found</h3>
                <p>Get started by creating your first movie</p>
                {onCreateNew && (
                    <Button
                        variant="primary"
                        onClick={onCreateNew}
                        className={styles.createButton}
                    >
                        Create First Movie
                    </Button>
                )}
            </div>
        );
    }

    return (
        <div className={styles.grid}>
            {movies.map(movie => (
                <MovieCard
                    key={movie.id}
                    movie={movie}
                    onEdit={onEdit}
                    onDelete={onDelete}
                />
            ))}
        </div>
    );
};