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

interface PublicSessionParams {
    searchTerm?: string;
    date?: string;
}

export const useSession = () => {
    const getAdminSessionsApi = useApi<PageResponse<SessionAdminResponse>>();
    const getPublicSessionsApi = useApi<SessionScheduleResponse[]>();
    const getAdminSessionByIdApi = useApi<SessionAdminResponse>();
    const getSessionSeatsApi = useApi<SeatReservationResponse>();
    const createSessionApi = useApi<SessionAdminResponse>();
    const updateSessionApi = useApi<SessionAdminResponse>();
    const cancelSessionApi = useApi<void>();
    const reactivateSessionApi = useApi<void>();
    const deleteSessionApi = useApi<void>();

    const rawLoading = getAdminSessionsApi.loading || getPublicSessionsApi.loading ||
        getAdminSessionByIdApi.loading || getSessionSeatsApi.loading ||
        createSessionApi.loading || updateSessionApi.loading ||
        cancelSessionApi.loading || reactivateSessionApi.loading || deleteSessionApi.loading;
    const loading = useDelayedLoading(rawLoading, { delay: 150, minDisplayTime: 300 });
    const error = !!(getAdminSessionsApi.error || getPublicSessionsApi.error ||
        getAdminSessionByIdApi.error || getSessionSeatsApi.error ||
        createSessionApi.error || updateSessionApi.error ||
        cancelSessionApi.error || reactivateSessionApi.error || deleteSessionApi.error);

    const getSessions = useCallback(async (params?: SessionParams) => {
        const response = await getAdminSessionsApi.execute(
            () => sessionApi.admin.getSessions(params),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getAdminSessionsApi]);

    const getPublicSessions = useCallback(async (params?: PublicSessionParams) => {
        const response = await getPublicSessionsApi.execute(
            () => sessionApi.public.getSessions(params?.searchTerm, params?.date),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getPublicSessionsApi]);

    const getAdminSessionById = useCallback(async (id: number) => {
        const response = await getAdminSessionByIdApi.execute(
            () => sessionApi.admin.getById(id),
            { showErrorNotification: true }
        );
        return response || null;
    }, [getAdminSessionByIdApi]);

    const getSessionSeats = useCallback(async (sessionId: number) => {
        const response = await getSessionSeatsApi.execute(
            () => sessionApi.public.getSeatAvailability(sessionId),
            { showErrorNotification: false }
        );
        return response || null;
    }, [getSessionSeatsApi]);

    const createSession = useCallback(async (request: SessionCreateRequest) => {
        const response = await createSessionApi.execute(
            () => sessionApi.admin.create(request),
            { successMessage: 'Session created successfully' }
        );
        return response || null;
    }, [createSessionApi]);

    const updateSession = useCallback(async (id: number, request: SessionUpdateRequest) => {
        const response = await updateSessionApi.execute(
            () => sessionApi.admin.update(id, request),
            { successMessage: 'Session updated successfully' }
        );
        return response || null;
    }, [updateSessionApi]);

    const cancelSession = useCallback(async (id: number) => {
        await cancelSessionApi.execute(
            () => sessionApi.admin.cancel(id),
            { successMessage: 'Session cancelled successfully' }
        );
    }, [cancelSessionApi]);

    const reactivateSession = useCallback(async (id: number) => {
        await reactivateSessionApi.execute(
            () => sessionApi.admin.reactivate(id),
            { successMessage: 'Session reactivated successfully' }
        );
    }, [reactivateSessionApi]);

    const deleteSession = useCallback(async (id: number) => {
        await deleteSessionApi.execute(
            () => sessionApi.admin.delete(id),
            { successMessage: 'Session deleted successfully' }
        );
    }, [deleteSessionApi]);

    const resetAll = useCallback(() => {
        getAdminSessionsApi.reset();
        getPublicSessionsApi.reset();
        getAdminSessionByIdApi.reset();
        getSessionSeatsApi.reset();
        createSessionApi.reset();
        updateSessionApi.reset();
        cancelSessionApi.reset();
        reactivateSessionApi.reset();
        deleteSessionApi.reset();
    }, [getAdminSessionsApi, getPublicSessionsApi, getAdminSessionByIdApi, getSessionSeatsApi, createSessionApi, updateSessionApi, cancelSessionApi, reactivateSessionApi, deleteSessionApi]);

    return {
        sessions: getAdminSessionsApi.data?.content || [],
        scheduleSessions: getPublicSessionsApi.data || [],
        adminSession: getAdminSessionByIdApi.data,
        sessionSeats: getSessionSeatsApi.data,
        pagination: getAdminSessionsApi.data,
        loading,
        error,
        getSessions,
        getPublicSessions,
        getAdminSessionById,
        getSessionSeats,
        createSession,
        updateSession,
        cancelSession,
        reactivateSession,
        deleteSession,
        resetAll,
    };
};