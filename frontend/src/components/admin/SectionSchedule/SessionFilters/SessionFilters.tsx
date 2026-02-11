import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useMovies } from '@/hooks/features/movies/useMovies';
import { useCinemaHalls } from '@/hooks/features/cinemaHalls/useCinemaHalls';
import { Input, Select, Button } from '@/components/ui';
import type { CinemaSessionStatus } from '@/types/session';
import type { MovieCardResponse } from '@/types/movie';
import type { CinemaHallResponse } from '@/types/cinemaHall';
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
}

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
    activeFilterCount
}) => {
    const { allHalls: halls } = useCinemaHalls();
    const { allMovies, loading: moviesLoading } = useMovies();
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [selectedMovieTitle, setSelectedMovieTitle] = useState('');
    const movieSearchRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        if (filters.movieId && allMovies.length > 0) {
            const selectedMovie = allMovies.find((movie: MovieCardResponse) => movie.id === filters.movieId);
            if (selectedMovie) {
                setSelectedMovieTitle(selectedMovie.title);
            }
        } else {
            setSelectedMovieTitle('');
        }
    }, [filters.movieId, allMovies]);

    useEffect(() => {
        const handleClickOutside = (event: MouseEvent) => {
            if (movieSearchRef.current && !movieSearchRef.current.contains(event.target as Node)) {
                setShowMovieResults(false);
            }
        };

        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const filteredMovies = useMemo(() => {
        if (!movieSearchTerm.trim()) {
            return allMovies.slice(0, 20);
        }

        const searchLower = movieSearchTerm.toLowerCase();
        return allMovies
            .filter((movie: MovieCardResponse) => movie.title.toLowerCase().includes(searchLower))
            .slice(0, 20);
    }, [allMovies, movieSearchTerm]);

    const handleDateFromChange = (value: string) => {
        onDateFromChange(value || undefined);
    };

    const handleDateToChange = (value: string) => {
        onDateToChange(value || undefined);
    };

    const handleHallChange = (value: string | number) => {
        onHallChange(value ? Number(value) : undefined);
    };

    const handleMovieInputClick = () => {
        setShowMovieResults(true);
    };

    const handleMovieInputChange = (value: string) => {
        setMovieSearchTerm(value);
        if (value === '') {
            onMovieChange(undefined);
            setSelectedMovieTitle('');
        }
        setShowMovieResults(true);
    };

    const handleMovieSelect = (movieId: number, movieTitle: string) => {
        onMovieChange(movieId);
        setSelectedMovieTitle(movieTitle);
        setMovieSearchTerm(movieTitle);
        setShowMovieResults(false);
    };

    const handleStatusChange = (value: string | number) => {
        onStatusChange(value as CinemaSessionStatus || undefined);
    };

    const getCurrentSortProperty = () => {
        return filters.sort?.split(',')[0] || 'startTime';
    };

    const getCurrentSortDirection = () => {
        return filters.sort?.split(',')[1] || 'asc';
    };

    const handleSortPropertyChange = (value: string | number) => {
        const direction = getCurrentSortDirection();
        const newSort = `${value as string},${direction}`;
        onSortChange(newSort);
    };

    const handleSortDirectionChange = (value: string | number) => {
        const property = getCurrentSortProperty();
        const newSort = `${property},${value as string}`;
        onSortChange(newSort);
    };

    const handleToggleSortOrder = () => {
        const property = getCurrentSortProperty();
        const currentDirection = getCurrentSortDirection();
        const newDirection = currentDirection === 'asc' ? 'desc' : 'asc';
        const newSort = `${property},${newDirection}`;
        onSortChange(newSort);
    };

    const handleClearMovieSearch = () => {
        setMovieSearchTerm('');
        setSelectedMovieTitle('');
        setShowMovieResults(false);
        onMovieChange(undefined);
    };

    const hallOptions = [
        { value: '', label: 'All halls' },
        ...halls.map((hall: CinemaHallResponse) => ({
            value: hall.id.toString(),
            label: hall.name
        }))
    ];

    const statusOptions = [
        { value: '', label: 'All statuses' },
        { value: 'SCHEDULED', label: 'Scheduled' },
        { value: 'ONGOING', label: 'Ongoing' },
        { value: 'COMPLETED', label: 'Completed' },
        { value: 'CANCELLED', label: 'Cancelled' }
    ];

    const sortOptions = [
        { value: 'startTime', label: 'Start Time' },
        { value: 'basePrice', label: 'Price' }
    ];

    const sortDirectionOptions = [
        { value: 'asc', label: 'Ascending' },
        { value: 'desc', label: 'Descending' }
    ];

    const displayValue = selectedMovieTitle || movieSearchTerm;

    return (
        <div className={styles.filters}>
            <div className={styles.filterGrid}>
                <div className={styles.filterGroup}>
                    <label className={styles.label}>Date From</label>
                    <div className={styles.inputContainer}>
                        <Input
                            type="date"
                            value={filters.dateFrom || ''}
                            onChange={handleDateFromChange}
                            className={styles.input}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Date To</label>
                    <div className={styles.inputContainer}>
                        <Input
                            type="date"
                            value={filters.dateTo || ''}
                            onChange={handleDateToChange}
                            className={styles.input}
                            min={filters.dateFrom}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Hall</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={filters.hallId?.toString() || ''}
                            onChange={handleHallChange}
                            options={hallOptions}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Status</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={filters.status || ''}
                            onChange={handleStatusChange}
                            options={statusOptions}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Sort By</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={getCurrentSortProperty()}
                            onChange={handleSortPropertyChange}
                            options={sortOptions}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Sort Direction</label>
                    <div className={styles.sortOrderContainer}>
                        <Select
                            value={getCurrentSortDirection()}
                            onChange={handleSortDirectionChange}
                            options={sortDirectionOptions}
                            className={styles.sortOrderSelect}
                        />
                        <Button
                            variant="secondary"
                            size="small"
                            onClick={handleToggleSortOrder}
                            className={styles.sortOrderToggle}
                            title="Toggle sort order"
                        >
                            ↕️
                        </Button>
                    </div>
                </div>
            </div>

            <div className={styles.movieFilterRow}>
                <div className={styles.movieFilterGroup}>
                    <label className={styles.label}>Movie</label>
                    <div className={styles.movieSearch} ref={movieSearchRef}>
                        <div className={styles.movieInputWrapper}>
                            <Input
                                type="text"
                                value={displayValue}
                                onChange={handleMovieInputChange}
                                onClick={handleMovieInputClick}
                                placeholder="Select or search movie..."
                                className={styles.movieInput}
                            />
                            {(filters.movieId || displayValue) && (
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
                                    filteredMovies.map((movie: MovieCardResponse) => (
                                        <div
                                            key={movie.id}
                                            className={`${styles.movieOption} ${filters.movieId === movie.id ? styles.selected : ''}`}
                                            onClick={() => handleMovieSelect(movie.id, movie.title)}
                                        >
                                            <div className={styles.movieTitle}>{movie.title}</div>
                                            <div className={styles.movieDetails}>
                                                {movie.durationMinutes} min
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className={styles.noResults}>
                                        {movieSearchTerm.trim() ? `No movies found for "${movieSearchTerm}"` : 'No movies available'}
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
                            <div className={styles.filterCount}>
                                <span className={styles.countBadge}>{activeFilterCount}</span>
                                active filter{activeFilterCount !== 1 ? 's' : ''}
                            </div>
                            <div className={styles.filterActions}>
                                <Button
                                    variant="error"
                                    size="medium"
                                    onClick={onClearFilters}
                                    className={styles.clearButton}
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