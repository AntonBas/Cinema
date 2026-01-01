import { useState, useEffect, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionAdminResponse, SessionFilter, SessionFilters } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseSessionsOptions {
    enabled?: boolean;
}

interface UseSessionsReturn {
    sessions: SessionAdminResponse[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionAdminResponse> | null;
    refetch: () => Promise<void>;
}

export const useSessions = (
    filters?: SessionFilters,
    searchParams?: SearchParams,
    options?: UseSessionsOptions
): UseSessionsReturn => {
    const [sessions, setSessions] = useState<SessionAdminResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionAdminResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const enabled = options?.enabled ?? true;

    const convertToApiFilter = useCallback((filters?: SessionFilters): SessionFilter => {
        const apiFilter: SessionFilter = {};

        if (filters?.date) {
            apiFilter.startDate = filters.date;
            apiFilter.endDate = filters.date;
        } else if (filters?.days) {
            const startDate = new Date();
            const endDate = new Date();
            endDate.setDate(endDate.getDate() + filters.days);

            apiFilter.startDate = startDate.toISOString().split('T')[0];
            apiFilter.endDate = endDate.toISOString().split('T')[0];
        }

        if (filters?.hallId) {
            apiFilter.hallId = filters.hallId;
        }

        if (filters?.movieId) {
            apiFilter.movieId = filters.movieId;
        }

        if (searchParams?.page !== undefined) {
            apiFilter.page = searchParams.page;
        }
        if (searchParams?.size !== undefined) {
            apiFilter.size = searchParams.size;
        }

        return apiFilter;
    }, [searchParams]);

    const loadSessions = useCallback(async () => {
        if (!enabled) return;

        setLoading(true);
        setError(null);

        try {
            const apiFilter = convertToApiFilter(filters);
            const response = await sessionApi.getFilteredSessions(apiFilter);

            setSessions(response.content);
            setPagination(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load sessions');
        } finally {
            setLoading(false);
        }
    }, [filters, enabled, convertToApiFilter]);

    useEffect(() => {
        loadSessions();
    }, [loadSessions]);

    const refetch = useCallback(async () => {
        await loadSessions();
    }, [loadSessions]);

    return {
        sessions,
        loading,
        error,
        pagination,
        refetch
    };
};