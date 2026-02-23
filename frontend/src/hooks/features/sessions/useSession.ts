import { useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useSession = () => {
    const sessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const scheduleApi = useApi<PageResponse<SessionScheduleResponse>>();
    const sessionDetailApi = useApi<SessionScheduleResponse>();

    const getSessions = useCallback(async (params?: SearchParams & SessionFilterRequest) => {
        return sessionsApi.execute(
            () => sessionApi.admin.getSessions(params),
            {
                cacheKey: params ? `admin_sessions_${JSON.stringify(params)}` : 'admin_sessions_all',
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionsApi]);

    const getPublicSessions = useCallback(async (
        searchTerm?: string,
        date?: string,
        params?: SearchParams
    ) => {
        return scheduleApi.execute(
            () => sessionApi.public.getSessions(searchTerm, date, params),
            {
                cacheKey: searchTerm || date
                    ? `public_sessions_${searchTerm || ''}_${date || ''}_${JSON.stringify(params)}`
                    : 'public_sessions_all',
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [scheduleApi]);

    const getSessionById = useCallback(async (id: number) => {
        return sessionDetailApi.execute(
            () => sessionApi.public.getById(id),
            {
                cacheKey: `session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
    }, [sessionDetailApi]);

    const getSessionSeats = useCallback(async (sessionId: number) => {
        return sessionDetailApi.execute(
            () => sessionApi.public.getSeatAvailability(sessionId),
            {
                cacheKey: `session_seats_${sessionId}`,
                cacheTime: 10 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionDetailApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        const result = await sessionsApi.execute(
            () => sessionApi.admin.create(request),
            {
                successMessage: 'Session created successfully',
            }
        );
        sessionsApi.invalidateCache();
        return result;
    }, [sessionsApi]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest) => {
        const result = await sessionsApi.execute(
            () => sessionApi.admin.update(id, request),
            {
                successMessage: 'Session updated successfully',
            }
        );
        sessionsApi.invalidateCache();
        sessionDetailApi.invalidateCache(`session_${id}`);
        return result;
    }, [sessionsApi, sessionDetailApi]);

    const cancelSession = useCallback(async (id: number) => {
        const result = await sessionsApi.execute(
            () => sessionApi.admin.cancel(id),
            {
                successMessage: 'Session cancelled successfully',
            }
        );
        sessionsApi.invalidateCache();
        sessionDetailApi.invalidateCache(`session_${id}`);
        return result;
    }, [sessionsApi, sessionDetailApi]);

    const reactivateSession = useCallback(async (id: number) => {
        const result = await sessionsApi.execute(
            () => sessionApi.admin.reactivate(id),
            {
                successMessage: 'Session reactivated successfully',
            }
        );
        sessionsApi.invalidateCache();
        sessionDetailApi.invalidateCache(`session_${id}`);
        return result;
    }, [sessionsApi, sessionDetailApi]);

    const deleteSession = useCallback(async (id: number) => {
        const result = await sessionsApi.execute(
            () => sessionApi.admin.delete(id),
            {
                successMessage: 'Session deleted successfully',
            }
        );
        sessionsApi.invalidateCache();
        sessionDetailApi.invalidateCache(`session_${id}`);
        return result;
    }, [sessionsApi, sessionDetailApi]);

    return {
        sessions: sessionsApi.data?.content || [],
        scheduleSessions: scheduleApi.data?.content || [],
        selectedSession: sessionDetailApi.data,
        pagination: sessionsApi.data,
        schedulePagination: scheduleApi.data,
        loading: sessionsApi.loading || scheduleApi.loading || sessionDetailApi.loading,
        error: sessionsApi.error || scheduleApi.error || sessionDetailApi.error,
        getSessions,
        getPublicSessions,
        getSessionById,
        getSessionSeats,
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,
        resetSessions: sessionsApi.reset,
        resetSchedule: scheduleApi.reset,
        resetSessionDetail: sessionDetailApi.reset,
        invalidateSessionsCache: sessionsApi.invalidateCache,
    };
};