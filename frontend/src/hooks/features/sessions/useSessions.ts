import { useState, useEffect, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionAdminResponse, CinemaSessionStatus } from '@/types/session';
import type { PageResponse } from '@/types/pagination';

interface UseSessionsOptions {
    enabled?: boolean;
    page?: number;
    size?: number;
    sort?: string;
    search?: string;
    date?: string;
    hallId?: number;
    movieId?: number;
    status?: CinemaSessionStatus;
}

interface UseSessionsReturn {
    sessions: SessionAdminResponse[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionAdminResponse> | null;
    refetch: () => Promise<void>;
}

export const useSessions = (
    options?: UseSessionsOptions
): UseSessionsReturn => {
    const [sessions, setSessions] = useState<SessionAdminResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionAdminResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const enabled = options?.enabled ?? true;

    const loadSessions = useCallback(async () => {
        if (!enabled) return;

        setLoading(true);
        setError(null);

        try {
            const response = await sessionApi.admin.getAll(
                options?.page,
                options?.size,
                options?.sort,
                options?.search,
                options?.date,
                options?.hallId,
                options?.movieId,
                options?.status
            );

            setSessions(response.content);
            setPagination(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load sessions');
            throw err;
        } finally {
            setLoading(false);
        }
    }, [enabled, options]);

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