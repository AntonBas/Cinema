import { useState, useEffect, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionDto, SessionFilters } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseSessionsReturn {
    sessions: SessionDto[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionDto> | null;
    refetch: () => void;
}

export const useSessions = (
    initialFilters?: SessionFilters,
    searchParams?: SearchParams
): UseSessionsReturn => {
    const [sessions, setSessions] = useState<SessionDto[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionDto> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadSessions = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            let response: PageResponse<SessionDto>;

            if (initialFilters?.date) {
                response = await sessionApi.getSessionsByDate(initialFilters.date, searchParams);
            } else if (initialFilters?.hallId) {
                response = await sessionApi.getSessionsByHall(initialFilters.hallId, searchParams);
            } else if (initialFilters?.movieId) {
                response = await sessionApi.getSessionsByMovie(initialFilters.movieId, searchParams);
            } else if (initialFilters?.days) {
                response = await sessionApi.getUpcomingSessions(initialFilters.days, searchParams);
            } else {
                response = await sessionApi.getAllSessions(searchParams);
            }

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