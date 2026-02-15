import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Input, Select, Button } from '@/components/ui';
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
        movieId?: number;
        status?: CinemaSessionStatus;
        sort?: string;
    };
    onDateFromChange: (dateFrom: string | undefined) => void;
    onDateToChange: (dateTo: string | undefined) => void;
    onHallChange: (hallId: number | undefined) => void;
    onMovieChange: (movieId: number | undefined) => void;
    onStatusChange: (status: CinemaSessionStatus | undefined) => void;
    onSortChange: (sort: string) => void;
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

const SORT_OPTIONS: SelectOption[] = [
    { value: 'startTime', label: 'Start Time' },
    { value: 'basePrice', label: 'Price' },
    { value: 'movie.title', label: 'Movie Title' },
    { value: 'hall.name', label: 'Hall' },
    { value: 'status', label: 'Status' },
    { value: 'ticketsSold', label: 'Tickets Sold' },
    { value: 'totalRevenue', label: 'Revenue' }
];

const SORT_DIRECTION_OPTIONS: SelectOption[] = [
    { value: 'asc', label: 'Ascending' },
    { value: 'desc', label: 'Descending' }
];

const DEBOUNCE_DELAY = 300;

export const SessionFilters: React.FC<SessionFiltersProps> = ({
    filters,
    onDateFromChange,
    onDateToChange,
    onHallChange,
    onMovieChange,
    onStatusChange,
    onSortChange,
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
    const [selectedMovieTitle, setSelectedMovieTitle] = useState('');
    const movieSearchRef = useRef<HTMLDivElement>(null);
    const debounceTimerRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        if (filters.movieId && movies.length > 0) {
            const selectedMovie = movies.find(
                (movie: MovieCardResponse) => movie.id === filters.movieId
            );
            if (selectedMovie) {
                setSelectedMovieTitle(selectedMovie.title);
                setMovieSearchTerm(selectedMovie.title);
                setDebouncedSearchTerm(selectedMovie.title);
            }
        } else {
            setSelectedMovieTitle('');
            setMovieSearchTerm('');
            setDebouncedSearchTerm('');
        }
    }, [filters.movieId, movies]);

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
            .filter((movie: MovieCardResponse) =>
                movie.title.toLowerCase().includes(searchLower)
            )
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
            onMovieChange(undefined);
            setSelectedMovieTitle('');
        }
        setShowMovieResults(true);
    }, [onMovieChange]);

    const handleMovieSelect = useCallback((movieId: number, movieTitle: string) => {
        onMovieChange(movieId);
        setSelectedMovieTitle(movieTitle);
        setMovieSearchTerm(movieTitle);
        setDebouncedSearchTerm(movieTitle);
        setShowMovieResults(false);
    }, [onMovieChange]);

    const handleStatusChange = useCallback((value: string | number) => {
        onStatusChange(value as CinemaSessionStatus || undefined);
    }, [onStatusChange]);

    const getCurrentSortProperty = useCallback((): string => {
        return filters.sort?.split(',')[0] || 'startTime';
    }, [filters.sort]);

    const getCurrentSortDirection = useCallback((): string => {
        return filters.sort?.split(',')[1] || 'asc';
    }, [filters.sort]);

    const handleSortPropertyChange = useCallback((value: string | number) => {
        const direction = getCurrentSortDirection();
        const newSort = `${value as string},${direction}`;
        onSortChange(newSort);
    }, [getCurrentSortDirection, onSortChange]);

    const handleSortDirectionChange = useCallback((value: string | number) => {
        const property = getCurrentSortProperty();
        const newSort = `${property},${value as string}`;
        onSortChange(newSort);
    }, [getCurrentSortProperty, onSortChange]);

    const handleToggleSortOrder = useCallback(() => {
        const property = getCurrentSortProperty();
        const currentDirection = getCurrentSortDirection();
        const newDirection = currentDirection === 'asc' ? 'desc' : 'asc';
        const newSort = `${property},${newDirection}`;
        onSortChange(newSort);
    }, [getCurrentSortProperty, getCurrentSortDirection, onSortChange]);

    const handleClearMovieSearch = useCallback(() => {
        setMovieSearchTerm('');
        setDebouncedSearchTerm('');
        setSelectedMovieTitle('');
        setShowMovieResults(false);
        onMovieChange(undefined);
    }, [onMovieChange]);

    const hallOptions: SelectOption[] = useMemo(() => [
        { value: '', label: 'All halls' },
        ...(halls || []).map((hall: CinemaHallResponse) => ({
            value: hall.id.toString(),
            label: hall.name
        }))
    ], [halls]);

    const displayValue = selectedMovieTitle || movieSearchTerm;
    const isMovieSelected = Boolean(filters.movieId);

    const activeFiltersSummary = useMemo(() => {
        const active: string[] = [];
        if (filters.dateFrom) active.push('Date From');
        if (filters.dateTo) active.push('Date To');
        if (filters.hallId) active.push('Hall');
        if (filters.movieId) active.push('Movie');
        if (filters.status) active.push('Status');
        return active;
    }, [filters]);

    useEffect(() => {
        if (filters.dateFrom || filters.dateTo || filters.hallId || filters.movieId || filters.status) {
            console.log('Filters changed:', filters);
        }
    }, [filters]);

    return (
        <div className={`${styles.filters} ${className || ''}`}>
            <div className={styles.filterGrid}>
                <div className={styles.filterGroup}>
                    <label htmlFor="dateFrom" className={styles.label}>Date From</label>
                    <div className={styles.inputContainer}>
                        <Input
                            id="dateFrom"
                            type="date"
                            value={filters.dateFrom || ''}
                            onChange={handleDateFromChange}
                            className={styles.input}
                            aria-label="Filter sessions from date"
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="dateTo" className={styles.label}>Date To</label>
                    <div className={styles.inputContainer}>
                        <Input
                            id="dateTo"
                            type="date"
                            value={filters.dateTo || ''}
                            onChange={handleDateToChange}
                            className={styles.input}
                            min={filters.dateFrom}
                            aria-label="Filter sessions to date"
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="hall" className={styles.label}>Hall</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={filters.hallId?.toString() || ''}
                            onChange={handleHallChange}
                            options={hallOptions}
                            disabled={hallsLoading}
                            aria-label="Filter by hall"
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="status" className={styles.label}>Status</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={filters.status || ''}
                            onChange={handleStatusChange}
                            options={STATUS_OPTIONS}
                            aria-label="Filter by session status"
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="sortBy" className={styles.label}>Sort By</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={getCurrentSortProperty()}
                            onChange={handleSortPropertyChange}
                            options={SORT_OPTIONS}
                            aria-label="Sort by field"
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="sortDirection" className={styles.label}>Sort Direction</label>
                    <div className={styles.sortOrderContainer}>
                        <Select
                            value={getCurrentSortDirection()}
                            onChange={handleSortDirectionChange}
                            options={SORT_DIRECTION_OPTIONS}
                            className={styles.sortOrderSelect}
                            aria-label="Sort direction"
                        />
                        <Button
                            variant="secondary"
                            size="small"
                            onClick={handleToggleSortOrder}
                            className={styles.sortOrderToggle}
                            title={`Switch to ${getCurrentSortDirection() === 'asc' ? 'descending' : 'ascending'} order`}
                            aria-label="Toggle sort direction"
                        >
                            ↕️
                        </Button>
                    </div>
                </div>
            </div>

            <div className={styles.movieFilterRow}>
                <div className={styles.movieFilterGroup}>
                    <label htmlFor="movieSearch" className={styles.label}>Movie</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <div className={styles.movieInputWrapper}>
                            <Input
                                id="movieSearch"
                                type="text"
                                value={displayValue}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder="Select or search movie..."
                                className={`${styles.movieInput} ${isMovieSelected ? styles.movieSelected : ''}`}
                                disabled={moviesLoading}
                                aria-label="Search movies"
                                aria-expanded={showMovieResults}
                                aria-autocomplete="list"
                                aria-controls="movie-results"
                            />
                            {(filters.movieId || displayValue) && (
                                <Button
                                    variant="error"
                                    size="small"
                                    onClick={handleClearMovieSearch}
                                    className={styles.clearMovieButton}
                                    aria-label="Clear movie selection"
                                    title="Clear movie selection"
                                >
                                    ×
                                </Button>
                            )}
                        </div>

                        {showMovieResults && (
                            <div
                                id="movie-results"
                                className={styles.movieResults}
                                role="listbox"
                                aria-label="Movie search results"
                            >
                                {moviesLoading ? (
                                    <div className={styles.loadingResults} role="status">
                                        Loading movies...
                                    </div>
                                ) : filteredMovies.length > 0 ? (
                                    filteredMovies.map((movie: MovieCardResponse) => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${filters.movieId === movie.id ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie.id, movie.title)}
                                            role="option"
                                            aria-selected={filters.movieId === movie.id}
                                            tabIndex={0}
                                            onKeyDown={(e) => {
                                                if (e.key === 'Enter' || e.key === ' ') {
                                                    e.preventDefault();
                                                    handleMovieSelect(movie.id, movie.title);
                                                }
                                            }}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>
                                                {movie.durationMinutes} min
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults} role="status">
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
                    <div className={styles.clearFiltersWrapper}>
                        <div className={styles.filterInfo}>
                            <div className={styles.filterCount} title={activeFiltersSummary.join(', ')}>
                                <span className={styles.countBadge}>{activeFilterCount}</span>
                                active filter{activeFilterCount !== 1 ? 's' : ''}
                            </div>
                            <div className={styles.filterActions}>
                                <Button
                                    variant="error"
                                    size="medium"
                                    onClick={onClearFilters}
                                    className={styles.clearButton}
                                    aria-label="Clear all filters"
                                >
                                    Clear All Filters
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};