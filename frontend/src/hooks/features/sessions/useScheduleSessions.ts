import { useState, useEffect, useCallback, useMemo } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import type { PageResponse } from '@/types/pagination';

interface UseScheduleSessionsOptions {
    page?: number;
    size?: number;
    date?: string;
    movieId?: number;
    daysAhead?: number;
    enabled?: boolean;
    keepPreviousData?: boolean;
}

interface UseScheduleSessionsReturn {
    sessions: SessionScheduleResponse[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionScheduleResponse> | null;
    refetch: () => Promise<void>;
    hasMore: boolean;
    loadMore: () => Promise<void>;
}

export const useScheduleSessions = (
    options?: UseScheduleSessionsOptions
): UseScheduleSessionsReturn => {
    const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionScheduleResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const enabled = options?.enabled ?? true;

    const loadSessions = useCallback(async (isLoadMore = false) => {
        if (!enabled) return;

        setLoading(true);
        setError(null);

        try {
            const pageToLoad = isLoadMore && pagination
                ? (pagination.number + 1)
                : (options?.page ?? 0);

            const response = await sessionApi.public.getSchedule(
                pageToLoad,
                options?.size,
                options?.date,
                options?.movieId,
                options?.daysAhead
            );

            if (isLoadMore && options?.keepPreviousData) {
                setSessions(prev => [...prev, ...response.content]);
            } else {
                setSessions(response.content);
            }

            setPagination(response);
        } catch (err) {
            if (!isLoadMore || !options?.keepPreviousData) {
                setError(err instanceof Error ? err.message : 'Failed to load schedule sessions');
            }
            throw err;
        } finally {
            setLoading(false);
        }
    }, [enabled, options, pagination]);

    useEffect(() => {
        loadSessions();
    }, [loadSessions]);

    const refetch = useCallback(async () => {
        await loadSessions();
    }, [loadSessions]);

    const loadMore = useCallback(async () => {
        if (pagination && !pagination.last) {
            await loadSessions(true);
        }
    }, [loadSessions, pagination]);

    const hasMore = useMemo(() => {
        return pagination ? !pagination.last : false;
    }, [pagination]);

    return {
        sessions,
        loading,
        error,
        pagination,
        refetch,
        hasMore,
        loadMore
    };
};