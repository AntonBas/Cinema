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
    activeFilterCount: number;
}

export const SessionFilters: React.FC<SessionFiltersProps> = ({
    filters,
    onDateChange,
    onHallChange,
    onMovieChange,
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

    const displayValue = selectedMovieTitle || movieSearchTerm;

    return (
        <div className={styles.filters}>
            <div className={styles.header}>
                <h3 className={styles.title}>Filter Sessions</h3>
                {hasActiveFilters && (
                    <div className={styles.filterCount}>
                        <span className={styles.countBadge}>{activeFilterCount}</span>
                        filter{activeFilterCount !== 1 ? 's' : ''} active
                    </div>
                )}
            </div>

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
                            value={filters.days?.toString() || ''}
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
                                        className={styles.movieOption}
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
                {hasActiveFilters && (
                    <Button
                        variant="error"
                        size="medium"
                        onClick={onClearFilters}
                        className={styles.clearButton}
                    >
                        Clear All Filters
                    </Button>
                )}
            </div>
        </div>
    );
};