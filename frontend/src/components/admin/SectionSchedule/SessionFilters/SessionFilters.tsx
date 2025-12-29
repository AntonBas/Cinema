import React, { useState, useEffect, useRef } from 'react';
import { useMovieSearch, useCinemaHalls } from '@/hooks/features';
import { Input, Select, Button } from '@/components/ui';
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
    const { allHalls: halls } = useCinemaHalls();
    const { movies, searchMovies } = useMovieSearch();
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        searchMovies({
            searchTerm: '',
            page: 0,
            size: 50
        });
    }, [searchMovies]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (movieSearchRef.current && !movieSearchRef.current.contains(event.target as Node)) {
                setShowMovieResults(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    useEffect(() => {
        searchMovies({
            searchTerm: movieSearchTerm,
            page: 0,
            size: 50
        });
    }, [movieSearchTerm, searchMovies]);

    const handleDateChange = (value: string) => {
        onDateChange(value || undefined);
    };

    const handleHallChange = (value: string | number) => {
        onHallChange(value ? Number(value) : undefined);
    };

    const handleMovieInputClick = () => {
        setShowMovieResults(true);
    };

    const handleMovieInputChange = (value: string) => {
        setMovieSearchTerm(value);
    };

    const handleMovieSelect = (movieId: number, movieTitle: string) => {
        onMovieChange(movieId);
        setMovieSearchTerm(movieTitle);
        setShowMovieResults(false);
    };

    const handleUpcomingDaysChange = (value: string | number) => {
        onUpcomingDaysChange(value ? Number(value) : undefined);
    };

    const handleClearMovieSearch = () => {
        setMovieSearchTerm('');
        setShowMovieResults(false);
        onMovieChange(undefined);
    };

    const upcomingDaysOptions = [
        { value: '', label: 'All days' },
        { value: '1', label: 'Next 1 day' },
        { value: '3', label: 'Next 3 days' },
        { value: '7', label: 'Next 7 days' },
        { value: '14', label: 'Next 14 days' }
    ];

    const hallOptions = [
        { value: '', label: 'All halls' },
        ...halls.map(hall => ({
            value: hall.id.toString(),
            label: hall.name
        }))
    ];

    return (
        <div className={styles.filters}>
            <div className={styles.filterGroup}>
                <label>Date</label>
                <div className={styles.inputContainer}>
                    <Input
                        type="date"
                        value={filters.date || ''}
                        onChange={handleDateChange}
                    />
                </div>
            </div>

            <div className={styles.filterGroup}>
                <label>Upcoming Days</label>
                <div className={styles.selectContainer}>
                    <Select
                        value={filters.days?.toString() || ''}
                        onChange={handleUpcomingDaysChange}
                        options={upcomingDaysOptions}
                    />
                </div>
            </div>

            <div className={styles.filterGroup}>
                <label>Hall</label>
                <div className={styles.selectContainer}>
                    <Select
                        value={filters.hallId?.toString() || ''}
                        onChange={handleHallChange}
                        options={hallOptions}
                    />
                </div>
            </div>

            <div className={styles.filterGroup}>
                <label>Movie</label>
                <div className={styles.movieSearch} ref={movieSearchRef}>
                    <div className={styles.movieInputWrapper}>
                        <Input
                            type="text"
                            value={movieSearchTerm}
                            onChange={handleMovieInputChange}
                            onClick={handleMovieInputClick}
                            placeholder="Select or search movie..."
                            className={styles.movieInput}
                        />
                        {filters.movieId && (
                            <Button
                                variant="error"
                                size="small"
                                onClick={handleClearMovieSearch}
                                className={styles.clearMovieButton}
                            >
                                ×
                            </Button>
                        )}
                    </div>

                    {showMovieResults && (
                        <div className={styles.movieResults}>
                            {movies.map(movie => (
                                <div
                                    key={movie.id}
                                    className={styles.movieOption}
                                    onClick={() => handleMovieSelect(movie.id, movie.title)}
                                >
                                    <div className={styles.movieTitle}>{movie.title}</div>
                                    <div className={styles.movieDetails}>
                                        {movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : 'N/A'} • {movie.durationMinutes} min
                                    </div>
                                </div>
                            ))}

                            {movies.length === 0 && (
                                <div className={styles.noResults}>No movies found</div>
                            )}
                        </div>
                    )}
                </div>
            </div>

            {hasActiveFilters && (
                <Button
                    variant="error"
                    size="medium"
                    onClick={onClearFilters}
                    className={styles.clearButton}
                >
                    Clear Filters
                </Button>
            )}
        </div>
    );
};