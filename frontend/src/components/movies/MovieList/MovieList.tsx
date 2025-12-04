import React from 'react';
import type { MovieCardResponse } from '@/types/movie';
import { MovieCard } from '../MovieCard';
import { LoadingSpinner } from '@/components/ui';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieCardResponse[];
    loading?: boolean;
    emptyMessage?: string;
}

export const MovieList: React.FC<MovieListProps> = ({
    movies,
    loading = false,
    emptyMessage = "No movies found"
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
                <h3>{emptyMessage}</h3>
                <p>Try checking back later for new releases.</p>
            </div>
        );
    }

    return (
        <div className={styles.grid}>
            {movies.map(movie => (
                <MovieCard key={movie.id} movie={movie} />
            ))}
        </div>
    );
};