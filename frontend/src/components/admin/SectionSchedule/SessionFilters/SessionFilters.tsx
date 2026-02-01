import React, { useState, useEffect, useRef } from 'react';
import { useMovies, useCinemaHalls } from '@/hooks/features';
import { Input, Select, Button } from '@/components/ui';
import type { CinemaSessionStatus } from '@/types/session';
import type { MovieCardResponse } from '@/types/movie';
import styles from './SessionFilters.module.css';

interface SessionFiltersProps {
    filters: {
        date?: string;
        hallId?: number;
        movieId?: number;
        status?: CinemaSessionStatus;
    };
    onDateChange: (date: string | undefined) => void;
    onHallChange: (hallId: number | undefined) => void;
    onMovieChange: (movieId: number | undefined) => void;
    onStatusChange: (status: CinemaSessionStatus | undefined) => void;
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
    onClearFilters,
    hasActiveFilters,
    activeFilterCount
}) => {
    const { allHalls: halls } = useCinemaHalls();
    const { movies, search, loading } = useMovies();
    const [movieSearchTerm, setMovieSearchTerm] = useState('');
    const [showMovieResults, setShowMovieResults] = useState(false);
    const [selectedMovieTitle, setSelectedMovieTitle] = useState('');
    const [localMovies, setLocalMovies] = useState<MovieCardResponse[]>(movies);
    const movieSearchRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        search({
            page: 0,
            size: 50
        });
    }, [search]);

    useEffect(() => {
        setLocalMovies(movies);
    }, [movies]);

    useEffect(() => {
        if (filters.movieId && localMovies.length > 0) {
            const selectedMovie = localMovies.find(movie => movie.id === filters.movieId);
            if (selectedMovie) {
                setSelectedMovieTitle(selectedMovie.title);
            }
        } else {
            setSelectedMovieTitle('');
        }
    }, [filters.movieId, localMovies]);

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
        const debounceTimer = setTimeout(() => {
            search({
                search: movieSearchTerm.trim(),
                page: 0,
                size: 50
            });
        }, 300);

        return () => clearTimeout(debounceTimer);
    }, [movieSearchTerm, search]);

    const filteredMovies = React.useMemo(() => {
        if (!movieSearchTerm.trim()) {
            return localMovies;
        }

        const searchLower = movieSearchTerm.toLowerCase();
        return localMovies.filter(movie =>
            movie.title.toLowerCase().includes(searchLower)
        );
    }, [localMovies, movieSearchTerm]);

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

    const handleClearMovieSearch = () => {
        setMovieSearchTerm('');
        setSelectedMovieTitle('');
        setShowMovieResults(false);
        onMovieChange(undefined);
        search({
            page: 0,
            size: 50
        });
    };

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
                                {loading ? (
                                    <div className={styles.loadingResults}>
                                        Loading movies...
                                    </div>
                                ) : filteredMovies.length > 0 ? (
                                    filteredMovies.map(movie => (
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
            )}
        </div>
    );
};