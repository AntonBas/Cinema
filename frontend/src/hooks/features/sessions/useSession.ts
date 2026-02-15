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
    const sessionByIdApi = useApi<SessionScheduleResponse>();
    const adminSessionByIdApi = useApi<SessionAdminResponse>();
    const createSessionApi = useApi<SessionAdminResponse>();
    const updateSessionApi = useApi<SessionAdminResponse>();
    const cancelSessionApi = useApi<void>();
    const reactivateSessionApi = useApi<void>();
    const deleteSessionApi = useApi<void>();

    const getSessions = useCallback(async (params?: SearchParams & SessionFilterRequest) => {
        const cacheKey = params ? `admin_sessions_${JSON.stringify(params)}` : 'admin_sessions_all';
        return sessionsApi.execute(
            () => sessionApi.admin.getSessions(params),
            {
                cacheKey,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionsApi]);

    const getPublicSessions = useCallback(async (params?: SearchParams & SessionFilterRequest) => {
        const cacheKey = params ? `public_sessions_${JSON.stringify(params)}` : 'public_sessions_all';
        return scheduleApi.execute(
            () => sessionApi.public.getSessions(params),
            {
                cacheKey,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [scheduleApi]);

    const getSessionById = useCallback(async (id: number) => {
        return sessionByIdApi.execute(
            () => sessionApi.public.getById(id),
            {
                cacheKey: `session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionByIdApi]);

    const getAdminSessionById = useCallback(async (id: number) => {
        return adminSessionByIdApi.execute(
            () => sessionApi.admin.getById(id),
            {
                cacheKey: `admin_session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [adminSessionByIdApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        return createSessionApi.execute(
            () => sessionApi.admin.create(request),
            {
                successMessage: 'Session created successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                },
            }
        );
    }, [createSessionApi, sessionsApi, scheduleApi]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest) => {
        return updateSessionApi.execute(
            () => sessionApi.admin.update(id, request),
            {
                successMessage: 'Session updated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [updateSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const cancelSession = useCallback(async (id: number) => {
        return cancelSessionApi.execute(
            () => sessionApi.admin.cancel(id),
            {
                successMessage: 'Session cancelled successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [cancelSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const reactivateSession = useCallback(async (id: number) => {
        return reactivateSessionApi.execute(
            () => sessionApi.admin.reactivate(id),
            {
                successMessage: 'Session reactivated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [reactivateSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const deleteSession = useCallback(async (id: number) => {
        return deleteSessionApi.execute(
            () => sessionApi.admin.delete(id),
            {
                successMessage: 'Session deleted successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [deleteSessionApi, sessionsApi, scheduleApi, adminSessionByIdApi, sessionByIdApi]);

    const clearCache = useCallback(() => {
        sessionsApi.invalidateCache();
        scheduleApi.invalidateCache();
        sessionByIdApi.invalidateCache();
        adminSessionByIdApi.invalidateCache();
        createSessionApi.invalidateCache();
        updateSessionApi.invalidateCache();
        cancelSessionApi.invalidateCache();
        reactivateSessionApi.invalidateCache();
        deleteSessionApi.invalidateCache();
    }, [
        sessionsApi, scheduleApi, sessionByIdApi, adminSessionByIdApi,
        createSessionApi, updateSessionApi, cancelSessionApi,
        reactivateSessionApi, deleteSessionApi
    ]);

    const loading = sessionsApi.loading || scheduleApi.loading ||
        sessionByIdApi.loading || adminSessionByIdApi.loading ||
        createSessionApi.loading || updateSessionApi.loading ||
        cancelSessionApi.loading || reactivateSessionApi.loading ||
        deleteSessionApi.loading;

    const error = !!(sessionsApi.error || scheduleApi.error ||
        sessionByIdApi.error || adminSessionByIdApi.error ||
        createSessionApi.error || updateSessionApi.error ||
        cancelSessionApi.error || reactivateSessionApi.error ||
        deleteSessionApi.error);

    return {
        sessions: sessionsApi.data?.content || [],
        scheduleSessions: scheduleApi.data?.content || [],
        session: sessionByIdApi.data,
        adminSession: adminSessionByIdApi.data,
        pagination: sessionsApi.data,
        schedulePagination: scheduleApi.data,

        loading,
        error,

        getSessions,
        getPublicSessions,
        getSessionById,
        getAdminSessionById,
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,
        clearCache,

        resetSessions: sessionsApi.reset,
        resetSchedule: scheduleApi.reset,
        resetSession: sessionByIdApi.reset,
        resetAdminSession: adminSessionByIdApi.reset,
    };
};