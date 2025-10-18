import React from 'react';
import type { MovieResponse } from '@/types/Movie';
import { MovieCard } from '../MovieCard';
import styles from './MovieList.module.css';

interface MovieListProps {
    movies: MovieResponse[];
    onEdit: (movie: MovieResponse) => void;
    onDelete: (movie: MovieResponse) => void;
}

export const MovieList: React.FC<MovieListProps> = ({
    movies,
    onEdit,
    onDelete
}) => {
    if (movies.length === 0) {
        return (
            <div className={styles.empty}>
                <div className={styles.emptyIcon}>🎬</div>
                <h3>No movies found</h3>
                <p>Get started by creating your first movie</p>
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