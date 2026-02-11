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
    const createSessionApi = useApi<SessionAdminResponse>();
    const updateSessionApi = useApi<SessionAdminResponse>();
    const cancelSessionApi = useApi<void>();
    const reactivateSessionApi = useApi<void>();
    const deleteSessionApi = useApi<void>();

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
                showErrorNotification: false,
            }
        );
    }, [adminSessionByIdApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        return createSessionApi.callApi(
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
        return updateSessionApi.callApi(
            () => sessionApi.admin.update(id, request),
            {
                successMessage: 'Session updated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    scheduleApi.invalidateCache();
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [updateSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const cancelSession = useCallback(async (id: number) => {
        return cancelSessionApi.callApi(
            () => sessionApi.admin.cancel(id),
            {
                successMessage: 'Session cancelled successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    scheduleApi.invalidateCache();
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [cancelSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const reactivateSession = useCallback(async (id: number) => {
        return reactivateSessionApi.callApi(
            () => sessionApi.admin.reactivate(id),
            {
                successMessage: 'Session reactivated successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    adminSessionByIdApi.invalidateCache(`admin_session_${id}`);
                    scheduleApi.invalidateCache();
                    sessionByIdApi.invalidateCache(`session_${id}`);
                },
            }
        );
    }, [reactivateSessionApi, sessionsApi, adminSessionByIdApi, scheduleApi, sessionByIdApi]);

    const deleteSession = useCallback(async (id: number) => {
        return deleteSessionApi.callApi(
            () => sessionApi.admin.delete(id),
            {
                successMessage: 'Session deleted successfully',
                onSuccess: () => {
                    sessionsApi.invalidateCache();
                    scheduleApi.invalidateCache();
                },
            }
        );
    }, [deleteSessionApi, sessionsApi, scheduleApi]);

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
    }, [sessionsApi, scheduleApi, sessionByIdApi, adminSessionByIdApi,
        createSessionApi, updateSessionApi, cancelSessionApi, reactivateSessionApi, deleteSessionApi]);

    return {
        sessions: sessionsApi.data?.content || [],
        scheduleSessions: scheduleApi.data?.content || [],
        session: sessionByIdApi.data,
        adminSession: adminSessionByIdApi.data,
        pagination: sessionsApi.data,
        schedulePagination: scheduleApi.data,

        loading: sessionsApi.state.isLoading || scheduleApi.state.isLoading ||
            sessionByIdApi.state.isLoading || adminSessionByIdApi.state.isLoading ||
            createSessionApi.state.isLoading || updateSessionApi.state.isLoading ||
            cancelSessionApi.state.isLoading || reactivateSessionApi.state.isLoading ||
            deleteSessionApi.state.isLoading,
        error: sessionsApi.state.isError || scheduleApi.state.isError ||
            sessionByIdApi.state.isError || adminSessionByIdApi.state.isError ||
            createSessionApi.state.isError || updateSessionApi.state.isError ||
            cancelSessionApi.state.isError || reactivateSessionApi.state.isError ||
            deleteSessionApi.state.isError,

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
        refetchSession: sessionByIdApi.refetch,
        refetchAdminSession: adminSessionByIdApi.refetch,
    };
};