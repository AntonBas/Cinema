import { useState, useEffect, useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type { SessionScheduleResponse } from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';

interface UseScheduleSessionsReturn {
    sessions: SessionScheduleResponse[];
    loading: boolean;
    error: string | null;
    pagination: PageResponse<SessionScheduleResponse> | null;
    refetch: () => void;
}

export const useScheduleSessions = (
    type: 'all' | 'today' | 'upcoming' | 'available' | 'date' | 'movie',
    options?: {
        date?: string;
        movieId?: number;
        days?: number;
    },
    searchParams?: SearchParams
): UseScheduleSessionsReturn => {
    const [sessions, setSessions] = useState<SessionScheduleResponse[]>([]);
    const [pagination, setPagination] = useState<PageResponse<SessionScheduleResponse> | null>(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const loadSessions = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            let response;
            switch (type) {
                case 'all':
                    response = await sessionApi.getScheduleSessions(searchParams);
                    break;
                case 'today':
                    response = await sessionApi.getTodayScheduleSessions(searchParams);
                    break;
                case 'upcoming':
                    response = await sessionApi.getUpcomingScheduleSessions(options?.days || 7, searchParams);
                    break;
                case 'available':
                    response = await sessionApi.getAvailableScheduleSessions(searchParams);
                    break;
                case 'date':
                    if (!options?.date) throw new Error('Date is required for date filter');
                    response = await sessionApi.getScheduleSessionsByDate(options.date, searchParams);
                    break;
                case 'movie':
                    if (!options?.movieId) throw new Error('Movie ID is required for movie filter');
                    response = await sessionApi.getScheduleSessionsByMovie(options.movieId, searchParams);
                    break;
                default:
                    response = await sessionApi.getScheduleSessions(searchParams);
            }

            setSessions(response.content);
            setPagination(response);
        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to load schedule sessions');
        } finally {
            setLoading(false);
        }
    }, [type, options, searchParams]);

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