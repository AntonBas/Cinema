import React from 'react';
import { useMovies, useHalls } from '@/hooks/features';
import type { SessionFilters as SessionFiltersType } from '@/types/session';
import styles from './SessionFilters.module.css';

interface SessionFiltersProps {
    filters: SessionFiltersType;
    onDateChange: (date: string | undefined) => void;
    onHallChange: (hallId: number | undefined) => void;
    onMovieChange: (movieId: number | undefined) => void;
    onUpcomingDaysChange: (days: number | undefined) => void;
    onClearFilters: () => void;
    hasActiveFilters: boolean;
}

export const SessionFilters: React.FC<SessionFiltersProps> = ({
    filters,
    onDateChange,
    onHallChange,
    onMovieChange,
    onUpcomingDaysChange,
    onClearFilters,
    hasActiveFilters
}) => {
    const { movies } = useMovies();
    const { halls } = useHalls();

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        onDateChange(e.target.value || undefined);
    };

    const handleHallChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onHallChange(e.target.value ? Number(e.target.value) : undefined);
    };

    const handleMovieChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onMovieChange(e.target.value ? Number(e.target.value) : undefined);
    };

    const handleUpcomingDaysChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        onUpcomingDaysChange(e.target.value ? Number(e.target.value) : undefined);
    };

    return (
        <div className={styles.filters}>
            <div className={styles.filterGroup}>
                <label htmlFor="date">Date</label>
                <input
                    id="date"
                    type="date"
                    value={filters.date || ''}
                    onChange={handleDateChange}
                    className={styles.input}
                />
            </div>

            <div className={styles.filterGroup}>
                <label htmlFor="upcomingDays">Upcoming Days</label>
                <select
                    id="upcomingDays"
                    value={filters.days || ''}
                    onChange={handleUpcomingDaysChange}
                    className={styles.select}
                >
                    <option value="">All upcoming</option>
                    <option value="1">Next 1 day</option>
                    <option value="3">Next 3 days</option>
                    <option value="7">Next 7 days</option>
                    <option value="14">Next 14 days</option>
                </select>
            </div>

            <div className={styles.filterGroup}>
                <label htmlFor="hall">Hall</label>
                <select
                    id="hall"
                    value={filters.hallId || ''}
                    onChange={handleHallChange}
                    className={styles.select}
                >
                    <option value="">All halls</option>
                    {halls.map(hall => (
                        <option key={hall.id} value={hall.id}>
                            {hall.name}
                        </option>
                    ))}
                </select>
            </div>

            <div className={styles.filterGroup}>
                <label htmlFor="movie">Movie</label>
                <select
                    id="movie"
                    value={filters.movieId || ''}
                    onChange={handleMovieChange}
                    className={styles.select}
                >
                    <option value="">All movies</option>
                    {movies.map(movie => (
                        <option key={movie.id} value={movie.id}>
                            {movie.title}
                        </option>
                    ))}
                </select>
            </div>

            {hasActiveFilters && (
                <button
                    onClick={onClearFilters}
                    className={styles.clearButton}
                >
                    Clear Filters
                </button>
            )}
        </div>
    );
};