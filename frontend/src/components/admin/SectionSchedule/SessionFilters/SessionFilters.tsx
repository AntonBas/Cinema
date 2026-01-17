import React, { useState, useEffect, useRef } from 'react';
import { useMovieSearch, useCinemaHalls } from '@/hooks/features';
import { Input, Select, Button } from '@/components/ui';
import type { CinemaSessionStatus } from '@/types/session';
import styles from './SessionFilters.module.css';

interface SessionFiltersProps {
    filters: {
        date?: string;
        hallId?: number;
        movieId?: number;
        daysAhead?: number;
        status?: CinemaSessionStatus;
    };
    onDateChange: (date: string | undefined) => void;
    onHallChange: (hallId: number | undefined) => void;
    onMovieChange: (movieId: number | undefined) => void;
    onStatusChange: (status: CinemaSessionStatus | undefined) => void;
    onUpcomingDaysChange: (daysAhead: number | undefined) => void;
    onClearFilters: () => void;
    hasActiveFilters: boolean;
    activeFilterCount: number;
}

export const SessionFilters: React.FC<SessionFiltersProps> = ({
    filters,
    onDateChange,
    onHallChange,
    onMovieChange,
    onStatusChange,
    onUpcomingDaysChange,
    onClearFilters,
    hasActiveFilters,
    activeFilterCount
}) => {
    const { allHalls: halls } = useCinemaHalls();
    const { movies, searchMovies } = useMovieSearch();
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [selectedMovieTitle, setSelectedMovieTitle] = useState('');
    const movieSearchRef = useRef<HTMLDivElement>(null);
    const [isSearching, setIsSearching] = useState(false);

    useEffect(() => {
        searchMovies({
            searchTerm: '',
            page: 0,
            size: 50
        });
    }, [searchMovies]);

    useEffect(() => {
        if (filters.movieId && movies.length > 0) {
            const selectedMovie = movies.find(movie => movie.id === filters.movieId);
            if (selectedMovie) {
                setSelectedMovieTitle(selectedMovie.title);
            }
        } else if (!filters.movieId) {
            setSelectedMovieTitle('');
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
        setIsSearching(true);
        const debounceTimer = setTimeout(() => {
            if (movieSearchTerm !== '') {
                searchMovies({
                    searchTerm: movieSearchTerm,
                    page: 0,
                    size: 50
                });
            }
            setIsSearching(false);
        }, 300);

        return () => clearTimeout(debounceTimer);
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
        if (value === '') {
            onMovieChange(undefined);
        }
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

    const handleUpcomingDaysChange = (value: string | number) => {
        onUpcomingDaysChange(value ? Number(value) : undefined);
    };

    const handleClearMovieSearch = () => {
        setMovieSearchTerm('');
        setSelectedMovieTitle('');
        setShowMovieResults(false);
        onMovieChange(undefined);
    };

    const upcomingDaysOptions = [
        { value: '', label: 'All days' },
        { value: '1', label: 'Next 1 day' },
        { value: '3', label: 'Next 3 days' },
        { value: '7', label: 'Next 7 days' },
        { value: '14', label: 'Next 14 days' },
        { value: '30', label: 'Next 30 days' }
    ];

    const hallOptions = [
        { value: '', label: 'All halls' },
        ...halls.map(hall => ({
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

    const displayValue = selectedMovieTitle || movieSearchTerm;

    return (
        <div className={styles.filters}>
            <div className={styles.filterGrid}>
                <div className={styles.filterGroup}>
                    <label className={styles.label}>Date</label>
                    <div className={styles.inputContainer}>
                        <Input
                            type="date"
                            value={filters.date || ''}
                            onChange={handleDateChange}
                            className={styles.input}
                        />
                    </div>
                </div>

                <div className={styles.filterGroup}>
                    <label className={styles.label}>Upcoming Days</label>
                    <div className={styles.selectContainer}>
                        <Select
                            value={filters.daysAhead?.toString() || ''}
                            onChange={handleUpcomingDaysChange}
                            options={upcomingDaysOptions}
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
                                {isSearching && (
                                    <div className={styles.loadingResults}>Searching...</div>
                                )}

                                {!isSearching && movies.map(movie => (
                                    <div
                                        key={movie.id}
                                        className={`${styles.movieOption} ${filters.movieId === movie.id ? styles.selected : ''}`}
                                        onClick={() => handleMovieSelect(movie.id, movie.title)}
                                    >
                                        <div className={styles.movieTitle}>{movie.title}</div>
                                        <div className={styles.movieDetails}>
                                            {movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : 'N/A'} • {movie.durationMinutes} min
                                        </div>
                                    </div>
                                ))}

                                {!isSearching && movies.length === 0 && (
                                    <div className={styles.noResults}>No movies found</div>
                                )}
                            </div>
                        )}
                    </div>
                </div>
            </div>

            <div className={styles.footer}>
                <div className={styles.footerContent}>
                    <div className={styles.filterInfo}>
                        {hasActiveFilters && (
                            <>
                                <div className={styles.filterCount}>
                                    <span className={styles.countBadge}>{activeFilterCount}</span>
                                    active filter{activeFilterCount !== 1 ? 's' : ''}
                                </div>
                                <Button
                                    variant="error"
                                    size="medium"
                                    onClick={onClearFilters}
                                    className={styles.clearButton}
                                >
                                    Clear All Filters
                                </Button>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};