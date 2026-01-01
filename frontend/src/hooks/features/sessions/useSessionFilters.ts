import { useState, useCallback, useMemo } from 'react';
import type { SessionFilters } from '@/types/session';

interface UseSessionFiltersReturn {
    filters: SessionFilters;
    setDateFilter: (date: string | undefined) => void;
    setHallFilter: (hallId: number | undefined) => void;
    setMovieFilter: (movieId: number | undefined) => void;
    setUpcomingDaysFilter: (days: number | undefined) => void;
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

    const setDateFilter = useCallback((date: string | undefined) => {
        updateFilters(prev => ({
            ...prev,
            date,
            ...(date && { days: undefined })
        }));
    }, [updateFilters]);

    const setHallFilter = useCallback((hallId: number | undefined) => {
        updateFilters(prev => ({ ...prev, hallId }));
    }, [updateFilters]);

    const setMovieFilter = useCallback((movieId: number | undefined) => {
        updateFilters(prev => ({ ...prev, movieId }));
    }, [updateFilters]);

    const setUpcomingDaysFilter = useCallback((days: number | undefined) => {
        updateFilters(prev => ({
            ...prev,
            days,
            ...(days && { date: undefined })
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
            filters.date || filters.hallId || filters.movieId || filters.days
        );
    }, [filters]);

    const activeFilterCount = useMemo(() => {
        let count = 0;
        if (filters.date) count++;
        if (filters.hallId) count++;
        if (filters.movieId) count++;
        if (filters.days) count++;
        return count;
    }, [filters]);

    return {
        filters,
        setDateFilter,
        setHallFilter,
        setMovieFilter,
        setUpcomingDaysFilter,
        clearFilters,
        resetToDefault,
        hasActiveFilters,
        activeFilterCount
    };
};