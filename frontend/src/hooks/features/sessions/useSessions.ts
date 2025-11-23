import { useState, useEffect, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionResponse, SessionFilters } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseSessionsReturn {
    sessions: SessionResponse[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionResponse> | null;
    refetch: () => void;
}

export const useSessions = (
    initialFilters?: SessionFilters,
    searchParams?: SearchParams
): UseSessionsReturn => {
    const [sessions, setSessions] = useState<SessionResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadSessions = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await sessionApi.getFilteredSessions(
                initialFilters?.date,
                initialFilters?.hallId,
                initialFilters?.movieId,
                initialFilters?.days,
                searchParams
            );

            setSessions(response.content);
            setPagination(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load sessions');
        } finally {
            setLoading(false);
        }
    }, [initialFilters, searchParams]);

    useEffect(() => {
        loadSessions();
    }, [loadSessions]);

    return {
        sessions,
        loading,
        error,
        pagination,
        refetch: loadSessions
    };
};