import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Button } from '@/components/ui/Button/Button';
import type { CinemaSessionStatus } from '@/types/session';
import type { MovieCardResponse } from '@/types/movie';
import type { CinemaHallResponse } from '@/types/cinemaHall';
import type { SelectOption } from '@/components/ui/Select/Select';
import styles from './SessionFilters.module.css';

interface SessionFiltersProps {
    filters: {
        dateFrom?: string;
        dateTo?: string;
        hallId?: number;
        movieTitle?: string;
        status?: CinemaSessionStatus;
    };
    onDateFromChange: (dateFrom: string | undefined) => void;
    onDateToChange: (dateTo: string | undefined) => void;
    onHallChange: (hallId: number | undefined) => void;
    onMovieTitleChange: (movieTitle: string | undefined) => void;
    onStatusChange: (status: CinemaSessionStatus | undefined) => void;
    onClearFilters: () => void;
    hasActiveFilters: boolean;
    activeFilterCount: number;
    halls?: CinemaHallResponse[];
    hallsLoading?: boolean;
    movies?: MovieCardResponse[];
    moviesLoading?: boolean;
    className?: string;
}

const STATUS_OPTIONS: SelectOption[] = [
    { value: '', label: 'All statuses' },
    { value: 'SCHEDULED', label: 'Scheduled' },
    { value: 'ONGOING', label: 'Ongoing' },
    { value: 'COMPLETED', label: 'Completed' },
    { value: 'CANCELLED', label: 'Cancelled' }
];

const DEBOUNCE_DELAY = 300;

export const SessionFilters: React.FC<SessionFiltersProps> = ({
    filters,
    onDateFromChange,
    onDateToChange,
    onHallChange,
    onMovieTitleChange,
    onStatusChange,
    onClearFilters,
    hasActiveFilters,
    activeFilterCount,
    halls = [],
    hallsLoading = false,
    movies = [],
    moviesLoading = false,
    className
}) => {
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [debouncedSearchTerm, setDebouncedSearchTerm] = useState('');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const movieSearchRef = useRef<HTMLDivElement>(null);
    const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        setMovieSearchTerm(filters.movieTitle || '');
        setDebouncedSearchTerm(filters.movieTitle || '');
    }, [filters.movieTitle]);

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
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        debounceTimerRef.current = setTimeout(() => {
            setDebouncedSearchTerm(movieSearchTerm);
        }, DEBOUNCE_DELAY);

        return () => {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
            }
        };
    }, [movieSearchTerm]);

    const filteredMovies = useMemo(() => {
        if (!movies.length) return [];

        if (!debouncedSearchTerm.trim()) {
            return movies.slice(0, 20);
        }

        const searchLower = debouncedSearchTerm.toLowerCase();
        return movies
            .filter(movie => movie.title.toLowerCase().includes(searchLower))
            .slice(0, 20);
    }, [movies, debouncedSearchTerm]);

    const handleDateFromChange = useCallback((value: string) => {
        onDateFromChange(value || undefined);
    }, [onDateFromChange]);

    const handleDateToChange = useCallback((value: string) => {
        onDateToChange(value || undefined);
    }, [onDateToChange]);

    const handleHallChange = useCallback((value: string | number) => {
        onHallChange(value ? Number(value) : undefined);
    }, [onHallChange]);

    const handleMovieInputClick = useCallback(() => {
        setShowMovieResults(true);
    }, []);

    const handleMovieInputChange = useCallback((value: string) => {
        setMovieSearchTerm(value);
        if (value === '') {
            onMovieTitleChange(undefined);
        }
        setShowMovieResults(true);
    }, [onMovieTitleChange]);

    const handleMovieSelect = useCallback((movieTitle: string) => {
        onMovieTitleChange(movieTitle);
        setMovieSearchTerm(movieTitle);
        setDebouncedSearchTerm(movieTitle);
        setShowMovieResults(false);
    }, [onMovieTitleChange]);

    const handleStatusChange = useCallback((value: string | number) => {
        onStatusChange(value as CinemaSessionStatus || undefined);
    }, [onStatusChange]);

    const handleClearMovieSearch = useCallback(() => {
        setMovieSearchTerm('');
        setDebouncedSearchTerm('');
        setShowMovieResults(false);
        onMovieTitleChange(undefined);
    }, [onMovieTitleChange]);

    const hallOptions: SelectOption[] = useMemo(() => {
        const hallsArray = Array.isArray(halls) ? halls : [];
        return [
            { value: '', label: 'All halls' },
            ...hallsArray.map(hall => ({
                value: hall.id.toString(),
                label: hall.name
            }))
        ];
    }, [halls]);

    return (
        <div className={`${styles.filters} ${className || ''}`}>
            <div className={styles.filterGrid}>
                <div className={styles.filterGroup}>
                    <label htmlFor="dateFrom" className={styles.label}>Date From</label>
                    <Input
                        id="dateFrom"
                        type="date"
                        value={filters.dateFrom || ''}
                        onChange={handleDateFromChange}
                        className={styles.filterInput}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="dateTo" className={styles.label}>Date To</label>
                    <Input
                        id="dateTo"
                        type="date"
                        value={filters.dateTo || ''}
                        onChange={handleDateToChange}
                        className={styles.filterInput}
                        min={filters.dateFrom}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="hall" className={styles.label}>Hall</label>
                    <div className={styles.filterSelectWrapper}>
                        <Select
                            value={filters.hallId?.toString() || ''}
                            onChange={handleHallChange}
                            options={hallOptions}
                            disabled={hallsLoading}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="status" className={styles.label}>Status</label>
                    <div className={styles.filterSelectWrapper}>
                        <Select
                            value={filters.status || ''}
                            onChange={handleStatusChange}
                            options={STATUS_OPTIONS}
                        />
                    </div>
                </div>
            </div>

            <div className={styles.movieFilterRow}>
                <div className={styles.movieFilterGroup}>
                    <label htmlFor="movieSearch" className={styles.label}>Movie Title</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <div className={styles.movieInputWrapper}>
                            <Input
                                id="movieSearch"
                                type="text"
                                value={movieSearchTerm}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder="Search by movie title..."
                                className={styles.movieInput}
                                disabled={moviesLoading}
                            />
                            {filters.movieTitle && (
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
                                {moviesLoading ? (
                                    <div className={styles.loadingResults}>
                                        Loading movies...
                                    </div>
                                ) : filteredMovies.length > 0 ? (
                                    filteredMovies.map(movie => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${filters.movieTitle === movie.title ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie.title)}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>
                                                {movie.durationMinutes} min
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults}>
                                        {debouncedSearchTerm.trim()
                                            ? `No movies found for "${debouncedSearchTerm}"`
                                            : 'No movies available'}
                                    </div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            {hasActiveFilters && (
                <div className={styles.clearFiltersContainer}>
                    <div className={styles.filterInfo}>
                        <span className={styles.countBadge}>{activeFilterCount}</span>
                        active filter{activeFilterCount !== 1 ? 's' : ''}
                        <Button
                            variant="error"
                            size="small"
                            onClick={onClearFilters}
                            className={styles.clearButton}
                        >
                            Clear All
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
};