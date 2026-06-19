import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Button } from '@/components/ui/Button/Button';
import { useMovies } from '@/hooks/features/movies/useMovies';
import type { CinemaSessionStatus } from '@/types/session';
import type { CinemaHallListResponse } from '@/types/cinemaHall';
import type { MovieSessionSearchResponse } from '@/types/movie';
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
    halls?: CinemaHallListResponse[];
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
    halls = [],
}) => {
    const [movieInput, setMovieInput] = useState(filters.movieTitle || '');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [filteredMovies, setFilteredMovies] = useState<MovieSessionSearchResponse[]>([]);
    const [isSearching, setIsSearching] = useState(false);
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    const { search } = useMovies();

    useEffect(() => {
        setMovieInput(filters.movieTitle || '');
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

    const handleMovieSearch = useCallback(async (query: string) => {
        setIsSearching(true);
        try {
            const results = await search(query);
            setFilteredMovies(results || []);
        } catch {
            setFilteredMovies([]);
        } finally {
            setIsSearching(false);
        }
    }, [search]);

    const handleMovieInputChange = useCallback((value: string) => {
        setMovieInput(value);
        setShowMovieResults(true);

        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        debounceTimerRef.current = setTimeout(() => {
            if (value.trim()) {
                handleMovieSearch(value);
            } else {
                setFilteredMovies([]);
            }
        }, DEBOUNCE_DELAY);
    }, [handleMovieSearch]);

    const handleMovieInputClick = useCallback(() => {
        if (movieInput.trim()) {
            handleMovieSearch(movieInput);
        }
        setShowMovieResults(true);
    }, [movieInput, handleMovieSearch]);

    const handleMovieSelect = useCallback((movieTitle: string) => {
        onMovieTitleChange(movieTitle);
        setMovieInput(movieTitle);
        setShowMovieResults(false);
    }, [onMovieTitleChange]);

    const handleClearMovie = useCallback(() => {
        setMovieInput('');
        onMovieTitleChange(undefined);
        setShowMovieResults(false);
        setFilteredMovies([]);
    }, [onMovieTitleChange]);

    const hallOptions: SelectOption[] = useMemo(() => [
        { value: '', label: 'All halls' },
        ...halls.map(hall => ({ value: hall.id.toString(), label: hall.name }))
    ], [halls]);

    const hasActiveFilters = !!(
        filters.dateFrom ||
        filters.dateTo ||
        filters.hallId ||
        filters.movieTitle ||
        filters.status
    );

    const activeFilterCount = [
        filters.dateFrom,
        filters.dateTo,
        filters.hallId,
        filters.movieTitle,
        filters.status
    ].filter(Boolean).length;

    return (
        <div className={styles.filters}>
            <div className={styles.filterGrid}>
                <div className={styles.filterGroup}>
                    <label htmlFor="dateFrom" className={styles.label}>Date From</label>
                    <Input
                        id="dateFrom"
                        type="date"
                        value={filters.dateFrom || ''}
                        onChange={(value) => onDateFromChange(value || undefined)}
                        className={styles.filterInput}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="dateTo" className={styles.label}>Date To</label>
                    <Input
                        id="dateTo"
                        type="date"
                        value={filters.dateTo || ''}
                        onChange={(value) => onDateToChange(value || undefined)}
                        className={styles.filterInput}
                        min={filters.dateFrom}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="hall" className={styles.label}>Hall</label>
                    <Select
                        value={filters.hallId?.toString() || ''}
                        onChange={(value) => onHallChange(value ? Number(value) : undefined)}
                        options={hallOptions}
                    />
                </div>

                <div className={styles.filterGroup}>
                    <label htmlFor="status" className={styles.label}>Status</label>
                    <Select
                        value={filters.status || ''}
                        onChange={(value) => onStatusChange(value as CinemaSessionStatus || undefined)}
                        options={STATUS_OPTIONS}
                    />
                </div>
            </div>

            <div className={styles.movieFilterRow}>
                <div className={styles.movieFilterGroup}>
                    <label htmlFor="movieTitle" className={styles.label}>Movie Title</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <div className={styles.movieInputWrapper}>
                            <Input
                                id="movieTitle"
                                type="text"
                                value={movieInput}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder="Search by movie title..."
                                className={styles.movieInput}
                            />
                            {movieInput && (
                                <Button
                                    variant="error"
                                    size="small"
                                    onClick={handleClearMovie}
                                    className={styles.clearMovieButton}
                                >
                                    ×
                                </Button>
                            )}
                        </div>

                        {showMovieResults && (
                            <div className={styles.movieResults}>
                                {isSearching ? (
                                    <div className={styles.loadingResults}>Loading movies...</div>
                                ) : filteredMovies.length > 0 ? (
                                    filteredMovies.map(movie => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${filters.movieTitle === movie.title ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie.title)}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>{movie.durationMinutes} min</div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults}>
                                        {movieInput.trim() ? `No movies found for "${movieInput}"` : 'Type to search movies'}
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
                        <Button variant="error" size="small" onClick={onClearFilters} className={styles.clearButton}>
                            Clear All
                        </Button>
                    </div>
                </div>
            )}
        </div>
    );
};