import { useState, useCallback } from 'react';
import type { SessionFilters } from '@/types/session';

interface UseSessionFiltersReturn {
    filters: SessionFilters;
    setDateFilter: (date: string | undefined) => void;
    setHallFilter: (hallId: number | undefined) => void;
    setMovieFilter: (movieId: number | undefined) => void;
    setUpcomingDaysFilter: (days: number | undefined) => void;
    clearFilters: () => void;
    hasActiveFilters: boolean;
}

export const useSessionFilters = (initialFilters: SessionFilters = {}): UseSessionFiltersReturn => {
    const [filters, setFilters] = useState<SessionFilters>(initialFilters);

    const setDateFilter = useCallback((date: string | undefined) => {
        setFilters(prev => ({
            ...prev,
            date,
            ...(date && { days: undefined })
        }));
    }, []);

    const setHallFilter = useCallback((hallId: number | undefined) => {
        setFilters(prev => ({ ...prev, hallId }));
    }, []);

    const setMovieFilter = useCallback((movieId: number | undefined) => {
        setFilters(prev => ({ ...prev, movieId }));
    }, []);

    const setUpcomingDaysFilter = useCallback((days: number | undefined) => {
        setFilters(prev => ({
            ...prev,
            days,
            ...(days && { date: undefined })
        }));
    }, []);

    const clearFilters = useCallback(() => {
        setFilters({});
    }, []);

    const hasActiveFilters = Boolean(
        filters.date || filters.hallId || filters.movieId || filters.days
    );

    return {
        filters,
        setDateFilter,
        setHallFilter,
        setMovieFilter,
        setUpcomingDaysFilter,
        clearFilters,
        hasActiveFilters
    };
};