import { useCallback } from 'react';
import { sessionApi } from '@/api/sessionApi';
import type {
    SessionAdminResponse,
    SessionScheduleResponse,
    SessionCreateRequest,
    SessionUpdateRequest,
    SessionFilterRequest
} from '@/types/session';
import type { SeatReservationResponse } from '@/types/seatReservation';
import type { PageResponse, SearchParams } from '@/types/pagination';
import { useApi } from '@/hooks/common/useApi';
import { useDelayedLoading } from '@/hooks/common/useDelayedLoading';

interface SessionParams extends SearchParams, SessionFilterRequest { }

interface PublicSessionParams extends SearchParams {
    searchTerm?: string;
    date?: string;
}

export const useSession = () => {
    const adminSessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const publicSessionsApi = useApi<PageResponse<SessionScheduleResponse>>();
    const adminSessionDetailApi = useApi<SessionAdminResponse>();
    const publicSessionDetailApi = useApi<SessionScheduleResponse>();
    const sessionSeatsApi = useApi<SeatReservationResponse>();
    const mutationApi = useApi<SessionAdminResponse | void>();

    const rawLoading = adminSessionsApi.loading || publicSessionsApi.loading ||
        adminSessionDetailApi.loading || publicSessionDetailApi.loading ||
        sessionSeatsApi.loading || mutationApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(adminSessionsApi.error || publicSessionsApi.error ||
        adminSessionDetailApi.error || publicSessionDetailApi.error ||
        sessionSeatsApi.error || mutationApi.error);

    const getSessions = useCallback(async (params?: SessionParams) => {
        const response = await adminSessionsApi.execute(
            () => sessionApi.admin.getSessions(params),
            {
                cacheKey: params ? `admin_sessions_${JSON.stringify(params)}` : 'admin_sessions_all',
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [adminSessionsApi]);

    const getPublicSessions = useCallback(async (params?: PublicSessionParams) => {
        const { searchTerm, date, ...restParams } = params || {};
        const response = await publicSessionsApi.execute(
            () => sessionApi.public.getSessions(searchTerm, date, restParams),
            {
                cacheKey: searchTerm || date
                    ? `public_sessions_${searchTerm || ''}_${date || ''}_${JSON.stringify(restParams)}`
                    : 'public_sessions_all',
                cacheTime: 30 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [publicSessionsApi]);

    const getAdminSessionById = useCallback(async (id: number) => {
        const response = await adminSessionDetailApi.execute(
            () => sessionApi.admin.getById(id),
            {
                cacheKey: `admin_session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [adminSessionDetailApi]);

    const getPublicSessionById = useCallback(async (id: number) => {
        const response = await publicSessionDetailApi.execute(
            () => sessionApi.public.getById(id),
            {
                cacheKey: `session_${id}`,
                cacheTime: 5 * 60 * 1000,
                showErrorNotification: true,
            }
        );
        return response || null;
    }, [publicSessionDetailApi]);

    const getSessionSeats = useCallback(async (sessionId: number) => {
        const response = await sessionSeatsApi.execute(
            () => sessionApi.public.getSeatAvailability(sessionId),
            {
                cacheKey: `session_seats_${sessionId}`,
                cacheTime: 10 * 1000,
                showErrorNotification: false,
            }
        );
        return response || null;
    }, [sessionSeatsApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        const response = await mutationApi.execute(
            () => sessionApi.admin.create(request),
            {
                successMessage: 'Session created successfully',
            }
        );
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        return response || null;
    }, [mutationApi, adminSessionsApi, publicSessionsApi]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest) => {
        const response = await mutationApi.execute(
            () => sessionApi.admin.update(id, request),
            {
                successMessage: 'Session updated successfully',
            }
        );
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        adminSessionDetailApi.invalidateCache(`admin_session_${id}`);
        publicSessionDetailApi.invalidateCache(`session_${id}`);
        return response || null;
    }, [mutationApi, adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi]);

    const cancelSession = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => sessionApi.admin.cancel(id),
            {
                successMessage: 'Session cancelled successfully',
            }
        );
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        adminSessionDetailApi.invalidateCache(`admin_session_${id}`);
        publicSessionDetailApi.invalidateCache(`session_${id}`);
    }, [mutationApi, adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi]);

    const reactivateSession = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => sessionApi.admin.reactivate(id),
            {
                successMessage: 'Session reactivated successfully',
            }
        );
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        adminSessionDetailApi.invalidateCache(`admin_session_${id}`);
        publicSessionDetailApi.invalidateCache(`session_${id}`);
    }, [mutationApi, adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi]);

    const deleteSession = useCallback(async (id: number) => {
        await mutationApi.execute(
            () => sessionApi.admin.delete(id),
            {
                successMessage: 'Session deleted successfully',
            }
        );
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        adminSessionDetailApi.invalidateCache(`admin_session_${id}`);
        publicSessionDetailApi.invalidateCache(`session_${id}`);
    }, [mutationApi, adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi]);

    const clearCache = useCallback(() => {
        adminSessionsApi.invalidateCache();
        publicSessionsApi.invalidateCache();
        adminSessionDetailApi.invalidateCache();
        publicSessionDetailApi.invalidateCache();
        sessionSeatsApi.invalidateCache();
        mutationApi.invalidateCache();
    }, [adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi, sessionSeatsApi, mutationApi]);

    const resetAll = useCallback(() => {
        adminSessionsApi.reset();
        publicSessionsApi.reset();
        adminSessionDetailApi.reset();
        publicSessionDetailApi.reset();
        sessionSeatsApi.reset();
        mutationApi.reset();
    }, [adminSessionsApi, publicSessionsApi, adminSessionDetailApi, publicSessionDetailApi, sessionSeatsApi, mutationApi]);

    return {
        sessions: adminSessionsApi.data?.content || [],
        scheduleSessions: publicSessionsApi.data?.content || [],
        adminSession: adminSessionDetailApi.data,
        publicSession: publicSessionDetailApi.data,
        sessionSeats: sessionSeatsApi.data,

        pagination: adminSessionsApi.data,
        schedulePagination: publicSessionsApi.data,

        loading,
        error,

        getSessions,
        getPublicSessions,
        getAdminSessionById,
        getPublicSessionById,
        getSessionSeats,
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,

        clearCache,
        resetAll,
    };
};