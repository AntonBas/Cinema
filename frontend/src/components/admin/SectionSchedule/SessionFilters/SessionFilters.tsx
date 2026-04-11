import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Input } from '@/components/ui/Input/Input';
import { Select } from '@/components/ui/Select/Select';
import { Button } from '@/components/ui/Button/Button';
import type { CinemaSessionStatus } from '@/types/session';
import type { CinemaHallListResponse } from '@/types/cinemaHall';
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
    const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

    useEffect(() => {
        setMovieInput(filters.movieTitle || '');
    }, [filters.movieTitle]);

    const handleMovieChange = useCallback((value: string) => {
        setMovieInput(value);

        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }

        debounceTimerRef.current = setTimeout(() => {
            onMovieTitleChange(value || undefined);
        }, DEBOUNCE_DELAY);
    }, [onMovieTitleChange]);

    const handleClearMovie = useCallback(() => {
        setMovieInput('');
        onMovieTitleChange(undefined);
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

            <div className={styles.filterGroup}>
                <label htmlFor="movieTitle" className={styles.label}>Movie Title</label>
                <div className={styles.movieInputWrapper}>
                    <Input
                        id="movieTitle"
                        type="text"
                        value={movieInput}
                        onChange={handleMovieChange}
                        placeholder="Search by movie title..."
                    />
                    {movieInput && (
                        <Button
                            variant="error"
                            size="small"
                            onClick={handleClearMovie}
                            className={styles.clearButton}
                        >
                            ×
                        </Button>
                    )}
                </div>
            </div>

            {hasActiveFilters && (
                <div className={styles.clearFiltersContainer}>
                    <span className={styles.countBadge}>{activeFilterCount}</span>
                    active filter{activeFilterCount !== 1 ? 's' : ''}
                    <Button variant="error" size="small" onClick={onClearFilters}>
                        Clear All
                    </Button>
                </div>
            )}
        </div>
    );
};