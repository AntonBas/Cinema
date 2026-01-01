import { useState, useEffect, useCallback, useMemo } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseScheduleSessionsOptions {
    date?: string;
    movieId?: number;
    days?: number;
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
    type: 'all' | 'today' | 'upcoming' | 'date' | 'movie',
    options?: UseScheduleSessionsOptions,
    searchParams?: SearchParams
): UseScheduleSessionsReturn => {
    const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionScheduleResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);
    const [isRefreshing, setIsRefreshing] = useState(false);

    const enabled = options?.enabled ?? true;

    const loadSessions = useCallback(async (isLoadMore = false) => {
        if (!enabled) return;

        setLoading(true);
        setError(null);

        if (isLoadMore && pagination) {
            setIsRefreshing(true);
        }

        try {
            const pageParams: SearchParams = isLoadMore && pagination
                ? { ...searchParams, page: pagination.currentPage + 1 }
                : { ...searchParams, page: 0 };

            let response;
            const today = new Date().toISOString().split('T')[0];

            switch (type) {
                case 'all':
                    response = await sessionApi.getScheduleSessions(
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
                    break;
                case 'today':
                    response = await sessionApi.getScheduleSessionsByDate(
                        today,
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
                    break;
                case 'upcoming':
                    response = await sessionApi.getUpcomingScheduleSessions(
                        options?.days || 7,
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
                    break;
                case 'date':
                    if (!options?.date) {
                        throw new Error('Date is required for date filter');
                    }
                    response = await sessionApi.getScheduleSessionsByDate(
                        options.date,
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
                    break;
                case 'movie':
                    if (!options?.movieId) {
                        throw new Error('Movie ID is required for movie filter');
                    }
                    response = await sessionApi.getScheduleSessionsByMovie(
                        options.movieId,
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
                    break;
                default:
                    response = await sessionApi.getScheduleSessions(
                        pageParams.page || 0,
                        pageParams.size || 20,
                        pageParams.sort || 'startTime'
                    );
            }

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
            setIsRefreshing(false);
        }
    }, [type, options, searchParams, enabled, pagination]);

    useEffect(() => {
        if (!isRefreshing) {
            loadSessions();
        }
    }, [loadSessions, isRefreshing]);

    const refetch = useCallback(async () => {
        await loadSessions();
    }, [loadSessions]);

    const loadMore = useCallback(async () => {
        if (pagination && pagination.currentPage < pagination.totalPages - 1) {
            await loadSessions(true);
        }
    }, [loadSessions, pagination]);

    const hasMore = useMemo(() => {
        return pagination ? pagination.currentPage < pagination.totalPages - 1 : false;
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