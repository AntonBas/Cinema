import { useState, useCallback, useMemo } from 'react';
import type { CinemaSessionStatus } from '@/types/session';

interface SessionFilters {
    search?: string;
    date?: string;
    hallId?: number;
    movieId?: number;
    daysAhead?: number;
    status?: CinemaSessionStatus;
}

interface UseSessionFiltersReturn {
    filters: SessionFilters;
    setSearchFilter: (search: string | undefined) => void;
    setDateFilter: (date: string | undefined) => void;
    setHallFilter: (hallId: number | undefined) => void;
    setMovieFilter: (movieId: number | undefined) => void;
    setStatusFilter: (status: CinemaSessionStatus | undefined) => void;
    setDaysAheadFilter: (daysAhead: number | undefined) => void;
    clearFilters: () => void;
    hasActiveFilters: boolean;
    activeFilterCount: number;
    resetToDefault: () => void;
}

const DEFAULT_FILTERS: SessionFilters = {};

export const useSessionFilters = (
    initialFilters: SessionFilters = DEFAULT_FILTERS,
    onFilterChange?: (filters: SessionFilters) => void
): UseSessionFiltersReturn => {
    const [filters, setFilters] = useState<SessionFilters>(initialFilters);

    const updateFilters = useCallback((updater: (prev: SessionFilters) => SessionFilters) => {
        setFilters(prev => {
            const newFilters = updater(prev);
            onFilterChange?.(newFilters);
            return newFilters;
        });
    }, [onFilterChange]);

    const setSearchFilter = useCallback((search: string | undefined) => {
        updateFilters(prev => ({ ...prev, search }));
    }, [updateFilters]);

    const setDateFilter = useCallback((date: string | undefined) => {
        updateFilters(prev => ({
            ...prev,
            date,
            daysAhead: undefined
        }));
    }, [updateFilters]);

    const setHallFilter = useCallback((hallId: number | undefined) => {
        updateFilters(prev => ({ ...prev, hallId }));
    }, [updateFilters]);

    const setMovieFilter = useCallback((movieId: number | undefined) => {
        updateFilters(prev => ({ ...prev, movieId }));
    }, [updateFilters]);

    const setStatusFilter = useCallback((status: CinemaSessionStatus | undefined) => {
        updateFilters(prev => ({ ...prev, status }));
    }, [updateFilters]);

    const setDaysAheadFilter = useCallback((daysAhead: number | undefined) => {
        updateFilters(prev => ({
            ...prev,
            daysAhead,
            date: undefined
        }));
    }, [updateFilters]);

    const clearFilters = useCallback(() => {
        updateFilters(() => ({}));
    }, [updateFilters]);

    const resetToDefault = useCallback(() => {
        updateFilters(() => DEFAULT_FILTERS);
    }, [updateFilters]);

    const hasActiveFilters = useMemo(() => {
        return Boolean(
            filters.search ||
            filters.date ||
            filters.hallId ||
            filters.movieId ||
            filters.daysAhead ||
            filters.status
        );
    }, [filters]);

    const activeFilterCount = useMemo(() => {
        let count = 0;
        if (filters.search) count++;
        if (filters.date) count++;
        if (filters.hallId) count++;
        if (filters.movieId) count++;
        if (filters.daysAhead) count++;
        if (filters.status) count++;
        return count;
    }, [filters]);

    return {
        filters,
        setSearchFilter,
        setDateFilter,
        setHallFilter,
        setMovieFilter,
        setStatusFilter,
        setDaysAheadFilter,
        clearFilters,
        resetToDefault,
        hasActiveFilters,
        activeFilterCount
    };
};