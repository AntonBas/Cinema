import { useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
} from '@/types/session';
import type { PageResponse } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';

export const useSession = () => {
    const sessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const scheduleApi = useApi<PageResponse<SessionScheduleResponse>>();
    const sessionByIdApi = useApi<SessionScheduleResponse>();
    const adminSessionByIdApi = useApi<SessionAdminResponse>();

    const getSessions = useCallback(async (params?: any) => {
        return sessionsApi.callApi(
            () => sessionApi.admin.getSessions(params),
            {
                cacheKey: `admin_sessions_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionsApi]);

    const getPublicSessions = useCallback(async (params?: any) => {
        return scheduleApi.callApi(
            () => sessionApi.public.getSessions(params),
            {
                cacheKey: `public_sessions_${JSON.stringify(params)}`,
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
    }, [scheduleApi]);

    const getSessionById = useCallback(async (id: number) => {
        return sessionByIdApi.callApi(
            () => sessionApi.public.getById(id),
            {
                cacheKey: `session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: false,
            }
        );
    }, [sessionByIdApi]);

    const getAdminSessionById = useCallback(async (id: number) => {
        return adminSessionByIdApi.callApi(
            () => sessionApi.admin.getById(id),
            {
                cacheKey: `admin_session_${id}`,
                cacheTime: 5 * 60 * 1000,
            }
        );
    }, [adminSessionByIdApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        const api = useApi<SessionAdminResponse>();
        return api.callApi(
            () => sessionApi.admin.create(request),
            {
                successMessage: 'Session created successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                },
            }
        );
    }, [sessionsApi]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest) => {
        const api = useApi<SessionAdminResponse>();
        return api.callApi(
            () => sessionApi.admin.update(id, request),
            {
                successMessage: 'Session updated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                },
            }
        );
    }, [sessionsApi, adminSessionByIdApi]);

    const cancelSession = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => sessionApi.admin.cancel(id),
            {
                successMessage: 'Session cancelled successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                },
            }
        );
    }, [sessionsApi, adminSessionByIdApi]);

    const reactivateSession = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => sessionApi.admin.reactivate(id),
            {
                successMessage: 'Session reactivated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                },
            }
        );
    }, [sessionsApi, adminSessionByIdApi]);

    const deleteSession = useCallback(async (id: number) => {
        const api = useApi<void>();
        return api.callApi(
            () => sessionApi.admin.delete(id),
            {
                successMessage: 'Session deleted successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                },
            }
        );
    }, [sessionsApi]);

    const clearCache = useCallback(() => {
        sessionsApi.invalidateCache();
        scheduleApi.invalidateCache();
        sessionByIdApi.invalidateCache();
        adminSessionByIdApi.invalidateCache();
    }, [sessionsApi, scheduleApi, sessionByIdApi, adminSessionByIdApi]);

    return {
        sessions: sessionsApi.data?.content || [],
        scheduleSessions: scheduleApi.data?.content || [],
        session: sessionByIdApi.data,
        adminSession: adminSessionByIdApi.data,
        pagination: sessionsApi.data,
        schedulePagination: scheduleApi.data,

        loading: sessionsApi.state.isLoading || scheduleApi.state.isLoading ||
            sessionByIdApi.state.isLoading || adminSessionByIdApi.state.isLoading,
        error: sessionsApi.state.isError || scheduleApi.state.isError ||
            sessionByIdApi.state.isError || adminSessionByIdApi.state.isError,

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
        refetchSessions: sessionsApi.refetch,
        refetchSchedule: scheduleApi.refetch,
    };
};